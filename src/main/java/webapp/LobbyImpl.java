package webapp;

import poker.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class LobbyImpl extends Observable implements Lobby, Observer {
    private final TableController tableController;
    private CompletableFuture<Map<Player, Integer>> winnersFuture;
    private TableController.State controllerState = null;
    private int lobbyTimeout = 4000;

    public LobbyImpl() {
        tableController = new TableController();
        tableController.addObserver(this);
    }

    public LobbyImpl(int lobbyTimeout) {
        tableController = new TableController();
        tableController.addObserver(this);
        this.lobbyTimeout = lobbyTimeout;
    }

    @Override
    public void start() {
        (new Thread(this::_start)).start();
    }

    private void _start() {
        while (true) {
            winnersFuture = CompletableFuture.supplyAsync(tableController::startRound).thenApply(x -> {
                x.forEach(Player::receiveWinnings);
                return x;
            });
            winnersFuture.join();
            setChanged();
            notifyObservers();
            try {
                Thread.sleep(lobbyTimeout);
            } catch (Exception ignored) {
            }
        }
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
    public Set<Player> getPlayerSet() {
        return new HashSet<>(tableController.getTable().getPlayers());
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
        if (controllerState == TableController.State.READY) {
            turnNotification = tableController.getTurnNotification();
        }

        Map<String, Boolean> foldedMap = new HashMap<>();
        GameData currentGameData = new GameData();

        currentGameData.setPot(table.getMainPotSize());
        currentGameData.setCommonCards(table.getCommonCards().toArray(new Card[0]));
        if (p == null) {
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
            if (winnersFuture.isDone()) {
                if (winnersFuture != null) {
                    currentGameData.setWinner(winnersFuture.get().keySet().toArray(new Player[0])[0]);
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
    public void awaitWinner() {
        winnersFuture.join();
    }

    @Override
    public void awaitReady() {
        tableController.getTurnNotification();
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
        if (arg instanceof TableController.State) {
            this.controllerState = (TableController.State) arg;
            if (arg != TableController.State.PROCESSING) {
                setChanged();
                notifyObservers();
            }
        }
    }

    @Override
    public TableController.State getControllerState() {
        return controllerState;
    }
}
