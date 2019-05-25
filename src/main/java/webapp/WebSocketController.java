package webapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
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
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import poker.Card;
import poker.Player;
import poker.Turn;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@EnableScheduling
@EnableAsync
@Controller

public class WebSocketController {
    private Map<String,Player> sessionIDMap = new HashMap<>();

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    private PokerController pokerController = new PokerController();

    @PostConstruct
    public void start() {
        Thread t = new Thread(() -> {
            try {
                pokerController.startGame();
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    @MessageMapping("/sendTurn")
    public void processTurnFromClient(@Payload String turn) throws Exception {
        System.out.println("TURN RECEIVED: " + turn);
        JSONObject messageAsJSON = new JSONObject(turn);

        String action = (String) messageAsJSON.getJSONObject("message").get("action");
        int amount = (int) messageAsJSON.getJSONObject("message").get("betAmount");
        String player = (String) messageAsJSON.getJSONObject("message").get("player");

        if(amount > pokerController.getTable().getPlayerFromName(player).getChips()){
            messagingTemplate.convertAndSend("/poker/" + player + "/error", "\"Can't bet more than you have.\"");
        }

        pokerController.setCurrentTurn(new Turn(
                pokerController.getTable().getPlayerFromName(player),
                Turn.PlayerAction.valueOf(action.toUpperCase()),
                amount));
    }

    private void sendGameData(Player p) throws JsonProcessingException {
        Map<String, Boolean> foldedMap = new HashMap<>();
        GameData currentGameData = new GameData();
        ObjectMapper mapper = new ObjectMapper();

        currentGameData.setPot(pokerController.getTable().getPotSize());
        currentGameData.setCommonCards(pokerController.getTable().getCommonCards().toArray(new Card[0]));
        currentGameData.setPersonalCards(p.getHole());
        currentGameData.setPlayers(pokerController.getTable().getPlayersInRound().keySet().toArray(new Player[0]));
        for (Map.Entry<Player, Boolean> entry : pokerController.getTable().getPlayersInRound().entrySet()) {
            foldedMap.put(entry.getKey().getName(), entry.getValue());
        }
        currentGameData.setFolded(foldedMap);
        if (pokerController.getCurrentTurnNotification() != null && p == pokerController.getCurrentTurnNotification().getPlayer()) {
            currentGameData.setTurnNotification(pokerController.getCurrentTurnNotification());
        }
        if (pokerController.getTable().getWinner() != null) {
            currentGameData.setWinner(pokerController.getTable().getWinner());
            currentGameData.setWinnerInfo(pokerController.winnerInfo);
        }
        messagingTemplate.convertAndSend("/poker/" + p.getName(), mapper.writeValueAsString(currentGameData));
    }

    @Scheduled(fixedRate = 1000)
    public void refreshAllPlayers() {
        for (Player p : pokerController.getTable().getPlayersInRound().keySet()) {
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
    public ResponseEntity login(@RequestBody String username){
        System.out.println(username);
        for(Player p : pokerController.getTable().getPlayersInRound().keySet()){
            if(p.getName().equals(username)) {
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        }
        pokerController.addPlayer(new Player(username, 250));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @EventListener
    public void onDisconnectEvent(SessionDisconnectEvent event) {
        pokerController.removePlayer(sessionIDMap.get(event.getSessionId()));
    }

    @EventListener
    public void onSubscribeEvent(SessionSubscribeEvent event) {
        String sessionID = event.getMessage().getHeaders().get("simpSessionId").toString();
        String name = event.getMessage().getHeaders().get("simpDestination").toString().substring(7);
        sessionIDMap.put(sessionID,pokerController.getTable().getPlayerFromName(name));
    }
}