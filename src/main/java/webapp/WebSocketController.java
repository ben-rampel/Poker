package webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import poker.Card;
import poker.Player;
import poker.Turn;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@EnableScheduling
@EnableAsync
@Controller

public class WebSocketController {
    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    private PokerController pokerController = new PokerController();

    @PostConstruct
    public void start(){
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pokerController.startGame();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    public void sendGameData(Player p) throws Exception {
        Thread.sleep(100); // simulated delay
        GameData currentGameData = new GameData();
        currentGameData.setPot(pokerController.getTable().getPotSize());
        currentGameData.setCommonCards(pokerController.getTable().getCommonCards().toArray(new Card[0]));
        currentGameData.setPersonalCards(p.getHole());
        currentGameData.setPlayers(pokerController.getTable().getPlayersInRound().keySet().toArray(new Player[0]));
        Map<String, Boolean> foldedMap = new HashMap<>();
        for(Map.Entry<Player, Boolean> entry: pokerController.getTable().getPlayersInRound().entrySet()){
            foldedMap.put(entry.getKey().getName(), entry.getValue());
        }
        currentGameData.setFolded(foldedMap);
        if (pokerController.getCurrentTurnNotification() != null && p == pokerController.getCurrentTurnNotification().getPlayer()) {
            currentGameData.setTurnNotification(pokerController.getCurrentTurnNotification());
        }
        if(pokerController.getTable().getWinner() != null){
            currentGameData.setWinner(pokerController.getTable().getWinner());
            currentGameData.setWinnerInfo(pokerController.winnerInfo);
        }
        ObjectMapper mapper = new ObjectMapper();
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

    @MessageMapping("/sendTurn")
    public String processTurnFromClient(@Payload String message) throws Exception {
        System.out.println(message);
        JSONObject messageAsJSON = new JSONObject(message);
        String turn = (String) messageAsJSON.getJSONObject("message").get("action");
        int amount = (int) messageAsJSON.getJSONObject("message").get("betAmount");
        String player = (String) messageAsJSON.getJSONObject("message").get("player");
        pokerController.setCurrentTurn(new Turn(
                pokerController.getTable().getPlayerFromName(player),
                Turn.PlayerAction.valueOf(turn.toUpperCase()),
                amount));
        return "";
    }

    @MessageExceptionHandler
    public String handleException(Throwable exception) {
        messagingTemplate.convertAndSend("/errors", exception.getMessage());
        return exception.getMessage();
    }
}