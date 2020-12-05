package poker;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface Table extends Iterator<Player> {
    List<Player> activePlayers();

    void drawHoleCards();

    List<Pot> getPots();

    void addPot(Pot pot);

    void addToPot(int i, Player player);

    void addPlayer(Player player);

    void removePlayer(Player player);

    void nextRound();

    void setCurrentBet(int amt);

    TableImpl.ROUND getRound();

    int getCurrentBet();

    boolean hasTwoNext();

    Map<Player, Integer> getShowdownWinners();

    Player getPlayerFromName(String name);

    Player getPlayerFromSessionID(String sessionID);

    List<Player> getPlayers();

    int getPotSize();

    List<Card> getCommonCards();

    void setWinnerInfo(String s);

    String getWinnerInfo();

}
