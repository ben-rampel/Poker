package poker;

import java.util.Iterator;
import java.util.List;

public interface Table extends Iterator<Player> {
    List<Player> activePlayers();

    void drawHoleCards();

    void addToPot(int i);

    void addPlayer(Player player);

    void removePlayer(Player player);

    void nextRound();

    void setCurrentBet(int amt);

    TableImpl.ROUND getRound();

    int getCurrentBet();

    boolean hasTwoNext();

    Player getShowdownWinner();

    Player getPlayerFromName(String name);

    Player getPlayerFromSessionID(String sessionID);

    List<Player> getPlayers();

    int getPotSize();

    List<Card> getCommonCards();

    String getWinnerInfo();
}
