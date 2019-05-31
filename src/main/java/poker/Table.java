package poker;

import java.util.*;


public class Table {
    public enum ROUND {PREFLOP, FLOP, TURN, RIVER, INTERIM}

    private ROUND round;
    private Deck deck;
    //Map the list of playerBets to their current bet in the round
    private Map<Player, Integer> playerBets;
    private Map<Player, Boolean> playersInRound;
    private List<Card> commonCards;
    private int potSize;
    private int dealerIndex;
    private int currentPlayerIndex;

    private TurnNotification currentTurn;

    private final int smallBlind;
    private final int bigBlind;

    private Player winner;

    public ROUND getRound() {
        return round;
    }

    public void setRound(ROUND round) {
        this.round = round;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public Map<Player, Integer> getPlayerBets() {
        return playerBets;
    }

    public void setPlayerBets(Map<Player, Integer> playerBets) {
        this.playerBets = playerBets;
    }

    public Map<Player, Boolean> getPlayersInRound() {
        return playersInRound;
    }

    public void setPlayersInRound(Map<Player, Boolean> playersInRound) {
        this.playersInRound = playersInRound;
    }

    public List<Card> getCommonCards() {
        return commonCards;
    }

    public void setCommonCards(List<Card> commonCards) {
        this.commonCards = commonCards;
    }

    public int getPotSize() {
        return potSize;
    }

    public void setPotSize(int potSize) {
        this.potSize = potSize;
    }

    public int getDealerIndex() {
        return dealerIndex;
    }

    public void setDealerIndex(int dealerIndex) {
        this.dealerIndex = dealerIndex;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public int getSmallBlind() {
        return smallBlind;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public Table(List<Player> players) {
        playerBets = new HashMap<>();
        playersInRound = new HashMap<>();
        if (players != null) {
            for (Player player : players) {
                playerBets.put(player, 0);
                playersInRound.put(player, true);
            }
        }
        dealerIndex = 0;
        commonCards = new ArrayList<>();
        potSize = 0;
        round = ROUND.PREFLOP;
        deck = new Deck();
        smallBlind = 1;
        bigBlind = 2;
    }

    public Table() {
        this(null);
    }

    public void addPlayer(Player p) {
        if (round != ROUND.INTERIM) {
            playersInRound.put(p, false);
        } else {
            playersInRound.put(p, true);
            playerBets.put(p, 0);
        }
    }

    public void removePlayer(Player p) {
        playersInRound.remove(p);
        playerBets.remove(p);
    }

    public Player getPlayerFromName(String name) {
        for (Player p : playersInRound.keySet()) {
            if (name.equals(p.getName()) || name.equals(p.getName().toLowerCase())) {
                return p;
            }
        }
        return null;
    }

    public Table resetTable() {
        Table newTable = new Table();
        for (Player p : this.playersInRound.keySet()) {
            newTable.playersInRound.put(p, true);
            newTable.playerBets.put(p, 0);
        }
        return newTable;
    }

}
