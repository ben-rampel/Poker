package webapp;

import poker.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class LobbyImpl implements Lobby, Observer {
    private final TableController tableController;
    private CompletableFuture<Map<Player, Integer>> winners;
    private TableController.State controllerState = null;

    public LobbyImpl(){
        tableController = new TableController();
        tableController.addObserver(this);
    }

    @Override
    public void start() {
        Thread gameThread = new Thread(() -> {
            while(true) {
                winners = CompletableFuture.supplyAsync(() -> {
                    try {
                        return tableController.startRound();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                });
                try {
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

    @Override
    public void addPlayer(Player player) {
        tableController.addPlayer(player);
    }

    @Override
    public void removePlayer(Player player) {
        tableController.removePlayer(player);
    }

    @Override
    public List<Player> getPlayers() {
        return tableController.getTable().getPlayers();
    }

    @Override
    public void receiveTurn(Turn turn) {
        tableController.receiveTurn(turn);
    }

    @Override
    public GameData getState() {
        return getState(null);
    }

    @Override
    public GameData getState(Player p) {
        Table table = tableController.getTable();
        TurnNotification turnNotification = null;
        if(controllerState == TableController.State.READY) {
            turnNotification = tableController.getTurnNotification();
        }

        Map<String, Boolean> foldedMap = new HashMap<>();
        GameData currentGameData = new GameData();

        currentGameData.setPot(table.getPotSize());
        currentGameData.setCommonCards(table.getCommonCards().toArray(new Card[0]));
        if (p == null){
            currentGameData.setPersonalCards(new Card[0]);
            currentGameData.setTurnNotification(turnNotification);
        } else {
            currentGameData.setPersonalCards(p.getHole());
            if (turnNotification != null && p == turnNotification.getPlayer()) {
                currentGameData.setTurnNotification(turnNotification);
            }
        }
        currentGameData.setSidePots(table.getPots().stream().filter(x -> !x.isMain()).toArray(Pot[]::new));
        currentGameData.setPlayers(table.getPlayers().toArray(new Player[0]));
        for (Player player : table.getPlayers()) {
            foldedMap.put(player.getName(), player.isInRound());
        }
        currentGameData.setFolded(foldedMap);

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
        return currentGameData;
    }

    @Override
    public Player getPlayerFromName(String name) {
        return tableController.getTable().getPlayerFromName(name);
    }

    @Override
    public Player getPlayerFromSessionID(String id) {
        return tableController.getTable().getPlayerFromSessionID(id);
    }

    @Override
    public void update(Observable o, Object arg) {
        if(arg instanceof TableController.State){
            this.controllerState = (TableController.State) arg;
            if(this.controllerState == TableController.State.READY) {
                System.out.println(getState());
            }
        }
    }

}
