package webapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import poker.Card;
import poker.Player;
import poker.Pot;
import poker.Turn;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@EnableScheduling
@EnableAsync
@Controller

public class WebSocketController {
    private final SimpMessageSendingOperations messagingTemplate;
    private final TableController tableController;
    private final UserRepository userRepository;
    private Future<Map<Player, Integer>> winners;

    @Autowired
    public WebSocketController(SimpMessageSendingOperations messagingTemplate, TableController tableController, UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.tableController = tableController;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public synchronized void start() throws InterruptedException {
        Thread gameThread = new Thread(() -> {
            while(true) {
                try {
                    winners = tableController.startRound();
                    Map<Player, Integer> winners_ = winners.get();
                    for (Map.Entry<Player, Integer> winner : winners_.entrySet()) {
                        winner.getKey().receiveWinnings(winner.getValue());
                    }
                    System.out.println("Game finished.");
                    Thread.sleep(4000);
                } catch (Exception ignored) {
                }
            }
        });
        gameThread.start();
    }

    @MessageMapping("/sendTurn")
    public void processTurnFromClient(@Payload String turn) throws Exception {
        System.out.println("TURN RECEIVED: " + turn);
        JSONObject messageAsJSON = new JSONObject(turn);

        String action = (String) messageAsJSON.getJSONObject("message").get("action");
        int amount = (int) messageAsJSON.getJSONObject("message").get("betAmount");
        String player = (String) messageAsJSON.getJSONObject("message").get("player");

        if (amount > tableController.getTable().getPlayerFromName(player).getChips()) {
            messagingTemplate.convertAndSend("/poker/" + player + "/error", "\"Can't bet more than you have.\"");
        }

        tableController.receiveTurn(new Turn(
                tableController.getTable().getPlayerFromName(player),
                Turn.PlayerAction.valueOf(action.toUpperCase()),
                amount));
    }

    private void sendGameData(Player p) throws JsonProcessingException {
        Map<String, Boolean> foldedMap = new HashMap<>();
        GameData currentGameData = new GameData();
        ObjectMapper mapper = new ObjectMapper();

        currentGameData.setPot(tableController.getTable().getPotSize());
        currentGameData.setCommonCards(tableController.getTable().getCommonCards().toArray(new Card[0]));
        currentGameData.setPersonalCards(p.getHole());
        //int l = tableController.getTable().getPots().size();
        currentGameData.setSidePots(tableController.getTable().getPots().stream().filter(x -> !x.isMain()).toArray(Pot[]::new));
        currentGameData.setPlayers(tableController.getTable().getPlayers().toArray(new Player[0]));
        for (Player player : tableController.getTable().getPlayers()) {
            foldedMap.put(player.getName(), player.isInRound());
        }
        currentGameData.setFolded(foldedMap);
        if (tableController.getTurnNotification() != null && p == tableController.getTurnNotification().getPlayer()) {
            currentGameData.setTurnNotification(tableController.getTurnNotification());
        }
        try {
            if (winners.isDone()) {
                if (winners != null) {
                    currentGameData.setWinner(winners.get().keySet().toArray(new Player[0])[0]);
                    currentGameData.setWinnerInfo(tableController.getTable().getWinnerInfo());
                } else {
                    currentGameData.setWinner(null);
                    currentGameData.setWinnerInfo("game ended");
                }
            }
        } catch (InterruptedException | ExecutionException | NullPointerException e) {
            e.printStackTrace();
        }
        messagingTemplate.convertAndSend("/poker/" + p.getName(), mapper.writeValueAsString(currentGameData));
    }

    @Scheduled(fixedRate = 1000)
    public void refreshAllPlayers() {
        for (Player p : tableController.getTable().getPlayers()) {
            try {
                sendGameData(p);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @MessageExceptionHandler
    public String handleException(Throwable exception) {
        System.out.println(exception.getMessage());
        //messagingTemplate.convertAndSend("/errors", exception.getMessage());
        return exception.getMessage();
    }

    @PostMapping("/login")
    public ResponseEntity<HttpStatus> login(@RequestBody String[] credentials) {
        System.out.println("login");
        String username = credentials[0];
        String password = credentials[1];
        for (Player p : tableController.getTable().getPlayers()) {
            if (p.getName().equals(username)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (userRepository.login(username, password)) {
            if (userRepository.getBalance(username) >= 250) {
                tableController.getTable().addPlayer(new Player(username, 250));
                userRepository.withdraw(username, 250);
            } else {
                tableController.getTable().addPlayer(new Player(username, 0));
            }
            if (tableController.getTable().activePlayers().size() > 2) {
                tableController.getTable().getPlayerFromName(username).setInRound(false);
            }
            tableController.unblock();
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/signUp")
    public String signUp(Model model) {
        model.addAttribute("signupform", new SignUpForm());
        return "signUp";
    }

    @PostMapping("/signUp")
    public String signUpSubmit(@ModelAttribute SignUpForm data, Model model) {
        userRepository.register(data.getName(), data.getPassword());
        System.out.println("registered");
        model.addAttribute("signupform", new SignUpForm());
        return "signUp";
    }

    @EventListener
    public void onDisconnectEvent(SessionDisconnectEvent event) {
        MessageHeaders headers = event.getMessage().getHeaders();
        String sessionID = (String) headers.get("simpSessionId");
        try {
            Player toBeRemoved = tableController.getTable().getPlayerFromSessionID(sessionID);
            userRepository.deposit(toBeRemoved.getName(), toBeRemoved.getChips());
            tableController.removePlayer(toBeRemoved);
            tableController.unblock();
        } catch (NoSuchElementException ignored) {}
    }

    @EventListener
    public void onSubscribeEvent(SessionSubscribeEvent event) {
        MessageHeaders headers = event.getMessage().getHeaders();
        if (!((String) headers.get("simpDestination")).contains("error")) {
            String sessionID = (String) headers.get("simpSessionId");
            String user = (((String) headers.get("simpDestination")).split("/")[2]);
            tableController.getTable().getPlayerFromName(user).setWebsocketsSession(sessionID);
        }
    }
}