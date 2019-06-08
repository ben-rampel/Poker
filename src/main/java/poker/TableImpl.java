package poker;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TableImpl implements Table {
    private ROUND round;
    private Deck deck;
    protected List<Card> commonCards;
    private List<Player> players;
    private int potSize;
    private int currentPlayerIndex;
    private int currentBet;
    private String winnerInfo;

    public TableImpl() {
        round = ROUND.BLINDS;
        deck = new Deck();
        commonCards = new ArrayList<>();
        players = new ArrayList<>();
        potSize = 0;
        currentPlayerIndex = 0;
    }

    public TableImpl(List<Player> players) {
        round = ROUND.BLINDS;
        deck = new Deck();
        commonCards = new ArrayList<>();
        this.players = players;
        potSize = 0;
        currentPlayerIndex = 0;
        for (Player p : players) {
            p.setDealer(false);
            p.setInRound(true);
            p.setBet(0);
            p.clearHole();
        }
    }

    @Override
    public void addPlayer(Player player) {
        players.add(player);
    }

    @Override
    public void removePlayer(Player player) {
        players.remove(player);
    }

    @Override
    public int getCurrentBet() {
        return currentBet;
    }

    @Override
    public void setCurrentBet(int amt) {
        this.currentBet = amt;
    }

    @Override
    public Player getShowdownWinner() {
        TreeMap<Hand, Player> bestHandMap = new TreeMap<>();
        for (Player p : activePlayers()) {
            bestHandMap.put(p.bestHand(commonCards.toArray(new Card[0])), p);
        }
        winnerInfo = bestHandMap.lastEntry().getValue().getName() + " wins with " + bestHandMap.lastEntry().getKey().toString();
        return bestHandMap.lastEntry().getValue();
    }

    @Override
    public void addToPot(int i) {
        potSize += i;
    }

    @Override
    public boolean hasNext() {
        return activePlayers().size() > 0;
    }

    @Override
    public boolean hasTwoNext() {
       return activePlayers().size() > 1;
    }

    @Override
    public Player next() {
        if (hasNext()) {
            int i = currentPlayerIndex + 1;
            Player p = activePlayers().get(adjustedIndex(i));
            for (; !p.isInRound(); i++) {
                p =  activePlayers().get(adjustedIndex(i));
            }
            currentPlayerIndex = i;
            if (!hasTwoNext()) {
                for (Player player : players) {
                    if (player.isInRound()) winnerInfo = player.getName() + " mucks";
                }
            }
            return p;
        } else {
            throw new IllegalStateException();
        }
    }

    private int adjustedIndex(int i) {
        return i % activePlayers().size();
    }

    public ROUND getRound() {
        return round;
    }

    @Override
    public void nextRound() {
        if (this.round != ROUND.BLINDS) {
            currentBet = 0;
        }

        if (this.round.getRoundNum() > ROUND.PREFLOP.getRoundNum() && activePlayers().size() > 2) {
            for (int i = 0; i < activePlayers().size(); i++) {
                if (activePlayers().get(adjustedIndex(i)).isDealer()) {
                    activePlayers().get(adjustedIndex(i)).setDealer(false);
                    activePlayers().get(adjustedIndex(i + 1)).setDealer(true);
                    next();
                    break;
                }
            }
        }

        if (this.round.getRoundNum() < 5) {
            this.round = ROUND.getRound(this.round.getRoundNum() + 1);
        } else {
            this.round = ROUND.PREFLOP;
        }

        switch (this.round) {
            case PREFLOP:
                for (Player p : activePlayers()) {
                    p.drawHole(deck);
                }
                break;
            case FLOP:
                for (int i = 0; i < 3; i++) {
                    commonCards.add(deck.next());
                }
                break;
            case TURN:
            case RIVER:
                commonCards.add(deck.next());
                break;
        }
        System.out.println("ROUND: " + round + " CARDS: " + commonCards);
        for (Player p : players) {
            if (p.isDealer()) System.out.println("dealer: " + p.getName());
        }
    }

    public int getPotSize() {
        return potSize;
    }

    public List<Card> getCommonCards() {
        return commonCards;
    }

    @Override
    public Player getPlayerFromName(String name) {
        for (Player player : players) if (player.getName().equals(name)) return player;
        throw new NoSuchElementException();
    }

    @Override
    public Player getPlayerFromSessionID(String sessionID) {
        for (Player player : players) if (player.getWebsocketsSession().equals(sessionID)) return player;
        throw new NoSuchElementException();    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public String getWinnerInfo() {
        return winnerInfo;
    }

    public enum ROUND {
        BLINDS(0), PREFLOP(1), FLOP(2), TURN(3), RIVER(4), INTERIM(5);

        private int roundNum;

        ROUND(int i) {
            this.roundNum = i;
        }

        public int getRoundNum() {
            return roundNum;
        }

        public static ROUND getRound(int i) {
            for (ROUND r : ROUND.values()) {
                if (r.roundNum == i) return r;
            }
            throw new IllegalArgumentException();
        }
    }

    public List<Player> activePlayers(){
        List<Player> results = new ArrayList<>();
        players.forEach(player -> {
            if(player.isInRound()) results.add(player);
        });
        return results;
    }

}
