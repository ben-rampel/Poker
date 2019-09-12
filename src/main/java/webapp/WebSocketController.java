package webapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.messaging.MessageHeaders;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import poker.Card;
import poker.Player;
import poker.Turn;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@EnableScheduling
@EnableAsync
@Controller

public class WebSocketController {
    private SimpMessageSendingOperations messagingTemplate;
    private TableController tableController;
    private UserRepository userRepository;
    private Future<Player> winner;
    private String discordID = "582421274219773962";
    private String discordToken = "kX3LOI-Y68fYI7cMk_koXl0uTn2qJNOP";
    private String discordRedirect = "http%3A%2F%2F24.211.132.87%3A8080%2FsignUp%2Fcallback";

    @Autowired
    public WebSocketController(SimpMessageSendingOperations messagingTemplate, TableController tableController, UserRepository userRepository) throws ClassNotFoundException {
        this.messagingTemplate = messagingTemplate;
        this.tableController = tableController;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void start() {
        Thread gameThread = new Thread(() -> {
            while (true) {
                try {
                    winner = tableController.startRound();
                    while (!winner.isDone()) {
                        Thread.sleep(1000);
                    }
                    if(winner != null) {
                        winner.get().receiveWinnings(tableController.getTable().getPotSize());
                    }
                    Thread.sleep(6000);
                } catch (Exception e) {
                    e.printStackTrace();
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
        currentGameData.setPlayers(tableController.getTable().getPlayers().toArray(new Player[0]));
        for (Player player : tableController.getTable().getPlayers()) {
            foldedMap.put(player.getName(), player.isInRound());
        }
        currentGameData.setFolded(foldedMap);
        if (tableController.getTurnNotification() != null && p == tableController.getTurnNotification().getPlayer()) {
            currentGameData.setTurnNotification(tableController.getTurnNotification());
        }
        try {
            if (winner.isDone()) {
                if(winner != null){
                    currentGameData.setWinner(winner.get());
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
    public ResponseEntity login(@RequestBody String[] credentials) {
        System.out.println("login");
        String username = credentials[0];
        String password = credentials[1];
        for (Player p : tableController.getTable().getPlayers()) {
            if (p.getName().equals(username)) return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        if (userRepository.login(username, password)) {
            if (userRepository.getBalance(username) >= 250) {
                tableController.getTable().addPlayer(new Player(username, 250));
                userRepository.withdraw(username, 250);
            } else {
                tableController.getTable().addPlayer(new Player(username, 0));
            }
            if(tableController.getTable().activePlayers().size() > 2) {
                tableController.getTable().getPlayerFromName(username).setInRound(false);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
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
        Player toBeRemoved = tableController.getTable().getPlayerFromSessionID(sessionID);
        userRepository.deposit(toBeRemoved.getName(), toBeRemoved.getChips());
        tableController.removePlayer(toBeRemoved);
    }

    @EventListener
    public void onSubscribeEvent(SessionSubscribeEvent event) {
        MessageHeaders headers = event.getMessage().getHeaders();
        if(!((String) headers.get("simpDestination")).contains("error")){
            String sessionID = (String) headers.get("simpSessionId");
            String user = (((String) headers.get("simpDestination")).split("/")[2]);
            tableController.getTable().getPlayerFromName(user).setWebsocketsSession(sessionID);
        }
    }
}