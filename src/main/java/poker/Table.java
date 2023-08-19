package poker;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface Table extends Iterator<Player> {
    List<Player> activePlayers();

    void drawHoleCards();

    List<Pot> getPots();

    Pot getLastPot();

    void splitLastPot(Player player, int amount);

    void addPot(Pot pot);

    void addToPot(Player player, int amount);

    void handleBet(Player player, int amount);

    void addPlayer(Player player);

    void removePlayer(Player player);

    void nextRound();

    void setRound(TableImpl.ROUND round);

    void setCurrentBet(int amt);

    TableImpl.ROUND getRound();

    int getCurrentBet();

    boolean hasTwoNext();

    Map<Player, Integer> getShowdownWinners();

    Player getPlayerFromName(String name);

    Player getPlayerFromSessionID(String sessionID);

    List<Player> getPlayers();

    int getMainPotSize();

    int getTotalPotAmount();

    List<Card> getCommonCards();

    void setWinnerInfo(String s);

    String getWinnerInfo();

}
