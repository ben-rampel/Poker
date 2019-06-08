package webapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import poker.Card;
import poker.Player;
import poker.Turn;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
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
    private Future<Player> winner;

    @Autowired
    public WebSocketController(SimpMessageSendingOperations messagingTemplate, TableController tableController) {
        this.messagingTemplate = messagingTemplate;
        this.tableController = tableController;
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
                    winner.get().receiveWinnings(tableController.getTable().getPotSize());
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
                currentGameData.setWinner(winner.get());
                currentGameData.setWinnerInfo(tableController.getTable().getWinnerInfo());
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
        String username = credentials[0];
        String password = credentials[1];
        for (Player p : tableController.getTable().getPlayers()) {
            if (p.getName().equals(username)) return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        if (UserDatabaseUtils.login(username, password)) {
            if (UserDatabaseUtils.getBalance(username) >= 250) {
                tableController.getTable().addPlayer(new Player(username, 250));
                UserDatabaseUtils.withdraw(username, 250);
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
        model.addAttribute("loggedIn", false);
        model.addAttribute("signupform", new SignUpForm());
        return "signUp";
    }

    @GetMapping("/signUp/callback")
    public String signUpCallBack(@RequestParam(required = false) String code, Model model) {
        if (code != null) {
            model.addAttribute("loggedIn", true);
            model.addAttribute("discordCode", code);
            model.addAttribute("signupform", new SignUpForm());
        }
        return "signUp";
    }

    @PostMapping("/signUp/callback")
    public String signUpSubmit(@ModelAttribute SignUpForm data, Model model) {
        String code = data.getDiscordCode();
        //System.out.println("code: " + code);
        try {
            URL url = new URL("https://discordapp.com/api/oauth2/token?grant_type=authorization_code&code=" + code + "&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2FsignUp%2Fcallback");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            String encoding = Base64.getEncoder().encodeToString(("582421274219773962:kX3LOI-Y68fYI7cMk_koXl0uTn2qJNOP").getBytes());
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Basic " + encoding);
            con.setFixedLengthStreamingMode(0);
            con.setDoOutput(true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            // System.out.println(content);
            JSONObject tokenObject = new JSONObject(content.toString());
            String token = (String) tokenObject.get("access_token");
            url = new URL("https://discordapp.com/api/users/@me");
            HttpURLConnection userDataCon = (HttpURLConnection) url.openConnection();
            userDataCon.setRequestProperty("Content-Type", "application/json");
            userDataCon.setRequestProperty("Authorization", "Bearer " + token);
            in = new BufferedReader(
                    new InputStreamReader(userDataCon.getInputStream()));
            content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            //System.out.println(content);
            JSONObject userdata = new JSONObject(content.toString());
            String discordID = (String) userdata.get("id");
            UserDatabaseUtils.register(data.getName(), data.getPassword(), discordID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("loggedIn", false);
        model.addAttribute("signupform", new SignUpForm());
        return "signUp";
    }

    @EventListener
    public void onDisconnectEvent(SessionDisconnectEvent event) {
        MessageHeaders headers = event.getMessage().getHeaders();
        String sessionID = (String) headers.get("simpSessionId");
        tableController.removePlayer(
                tableController.getTable().getPlayerFromSessionID(sessionID)
        );
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