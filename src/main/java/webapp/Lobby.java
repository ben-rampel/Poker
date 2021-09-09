package webapp;

import poker.Player;
import poker.Turn;

import java.util.List;
import java.util.Set;

public interface Lobby {
    void start();

    void addPlayer(Player player);

    void removePlayer(Player player);

    List<Player> getPlayers();

    Set<Player> getPlayerSet();

    void receiveTurn(Turn turn);

    void awaitWinner();

    void awaitReady();

    TableController.State getControllerState();

    GameData getState(Player player);

    GameData getState();

    Player getPlayerFromName(String name);

    Player getPlayerFromSessionID(String id);
}
