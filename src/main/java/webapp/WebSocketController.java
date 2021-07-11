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
import poker.Player;
import poker.Turn;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

@SuppressWarnings("SameReturnValue")
@EnableScheduling
@EnableAsync
@Controller

public class WebSocketController {
    private final SimpMessageSendingOperations messagingTemplate;
    private final List<Lobby> lobbies;
    private final UserRepository userRepository;

    @Autowired
    public WebSocketController(SimpMessageSendingOperations messagingTemplate, UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.lobbies = new LinkedList<>();
        this.lobbies.add(new LobbyImpl());
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void start() {
        lobbies.get(0).start();
    }

    @MessageMapping("/sendTurn")
    public void processTurnFromClient(@Payload String turn) throws Exception {
        JSONObject messageAsJSON = new JSONObject(turn);

        String action = (String) messageAsJSON.getJSONObject("message").get("action");
        int amount = (int) messageAsJSON.getJSONObject("message").get("betAmount");
        String player = (String) messageAsJSON.getJSONObject("message").get("player");

        try {
            lobbies.get(0).receiveTurn(new Turn(
                    lobbies.get(0).getPlayerFromName(player),
                    Turn.PlayerAction.valueOf(action.toUpperCase()),
                    amount));
        } catch (RuntimeException e){
            messagingTemplate.convertAndSend("/poker/" + player + "/error", "\"" + e.getMessage() + "\"");
        }
    }

    private void sendGameData(Player p) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        GameData state = lobbies.get(0).getState(p);
        messagingTemplate.convertAndSend("/poker/" + p.getName(), mapper.writeValueAsString(state));
    }

    @Scheduled(fixedRate = 400)
    public void refreshAllPlayers() {
        for (Player p : lobbies.get(0).getPlayers()) {
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
        for (Player p : lobbies.get(0).getPlayers()) {
            if (p.getName().equals(username)) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (userRepository.login(username, password)) {
            if (userRepository.getBalance(username) >= 250) {
                lobbies.get(0).addPlayer(new Player(username, 250));
                userRepository.withdraw(username, 250);
            } else {
                lobbies.get(0).addPlayer(new Player(username, 0));
            }
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
            Player toBeRemoved = lobbies.get(0).getPlayerFromSessionID(sessionID);
            userRepository.deposit(toBeRemoved.getName(), toBeRemoved.getChips());
            lobbies.get(0).removePlayer(toBeRemoved);
        } catch (NoSuchElementException ignored) {}
    }

    @EventListener
    public void onSubscribeEvent(SessionSubscribeEvent event) {
        MessageHeaders headers = event.getMessage().getHeaders();
        if (!((String) headers.get("simpDestination")).contains("error")) {
            String sessionID = (String) headers.get("simpSessionId");
            String user = (((String) headers.get("simpDestination")).split("/")[2]);
            lobbies.get(0).getPlayerFromName(user).setWebsocketsSession(sessionID);
        }
    }
}