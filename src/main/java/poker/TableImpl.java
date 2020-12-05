package poker;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TableImpl implements Table {
    private ROUND round;
    private Deck deck;
    protected List<Card> commonCards;
    private List<Player> players;
    private int currentPlayerIndex;
    private String winnerInfo;
    private List<Pot> pots;
    private int lastActivePlayerSize;


    public void setWinnerInfo(String winnerInfo) {
        this.winnerInfo = winnerInfo;
    }

    public TableImpl() {
        round = ROUND.BLINDS;
        deck = new Deck();
        commonCards = new ArrayList<>();
        players = new ArrayList<>();
        pots = new LinkedList<>();
        Pot main = new Pot(0);
        main.setMain(true);
        pots.add(main);
        lastActivePlayerSize = 0;
        currentPlayerIndex = 0;
    }

    public TableImpl(List<Player> players, int dealerSeed) {
        pots = new LinkedList<>();
        Pot main = new Pot(0);
        main.setMain(true);
        pots.add(main);
        round = ROUND.BLINDS;
        deck = new Deck();
        lastActivePlayerSize = 0;
        commonCards = new ArrayList<>();
        this.players = players;
        currentPlayerIndex = dealerSeed;
        for (Player p : players) {
            p.setDealer(false);
            p.setInRound(true);
            p.setBet(0);
            p.clearHole();
        }
    }

    @Override
    public void drawHoleCards() {
        for (Player p : activePlayers()) {
            p.drawHole(deck);
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
        return this.pots.get(0).getBet();
    }

    @Override
    public void setCurrentBet(int amt) {
        this.pots.get(0).setBet(amt);
    }

    @Override
    public Map<Player, Integer> getShowdownWinners() {
        Map<Player, Integer> results = new LinkedHashMap<>();
        for (int i = 0; i < pots.size(); i++) {
            TreeMap<Hand, Player> bestHandMap = new TreeMap<>();
            for (Player p : pots.get(i).getPlayers()) {
                bestHandMap.put(p.bestHand(commonCards.toArray(new Card[0])), p);
            }
            Player best = bestHandMap.lastEntry().getValue();
            if (results.containsKey(best)) {
                results.replace(best, results.get(best) + pots.get(i).getAmount());
            } else {
                results.put(best, pots.get(i).getAmount());
            }
            if (i == 0)
                winnerInfo = bestHandMap.lastEntry().getValue().getName() + " wins with " + bestHandMap.lastEntry().getKey().toString();
        }

        return results;
    }

    @Override
    public void addToPot(int i, Player p) {
        pots.get(0).add(p, i);
    }

    @Override
    public List<Pot> getPots() {
        return this.pots;
    }

    @Override
    public void addPot(Pot pot) {
        this.pots.add(pot);
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
            currentPlayerIndex = (currentPlayerIndex + 1) % activePlayers().size();
            if (lastActivePlayerSize > activePlayers().size()) {
                currentPlayerIndex -= (lastActivePlayerSize - activePlayers().size());
            }
            lastActivePlayerSize = activePlayers().size();
            return activePlayers().get(currentPlayerIndex);
        } else {
            throw new IllegalStateException();
        }
    }

    public ROUND getRound() {
        return round;
    }

    @Override
    public void nextRound() {
        for (Player p : activePlayers()) {
            p.setBet(0);
        }

        if (this.round != ROUND.BLINDS) {
            this.setCurrentBet(0);
        }

       /* if (this.round.getRoundNum() > ROUND.PREFLOP.getRoundNum() && activePlayers().size() > 2) {
            for (int i = 0; i < activePlayers().size(); i++) {
                if (activePlayers().get(adjustedIndex(i)).isDealer()) {
                    activePlayers().get(adjustedIndex(i)).setDealer(false);
                    activePlayers().get(adjustedIndex(i + 1)).setDealer(true);
                    next();
                    break;
                }
            }
        }*/

        if (this.round.getRoundNum() < 5) {
            this.round = ROUND.getRound(this.round.getRoundNum() + 1);
        } else {
            this.round = ROUND.PREFLOP;
        }

        switch (this.round) {
            case PREFLOP:
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
        return pots.get(0).getAmount();
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
        throw new NoSuchElementException();
    }

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

    public List<Player> activePlayers() {
        List<Player> results = new ArrayList<>();
        players.forEach(player -> {
            if (player.isInRound()) results.add(player);
        });
        return results;
    }
}
