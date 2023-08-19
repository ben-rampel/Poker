package poker;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TableImpl implements Table {
    private ROUND round;
    private final Deck deck;
    protected List<Card> commonCards;
    private final List<Player> players;
    private int currentPlayerIndex;
    private String winnerInfo;
    private final List<Pot> pots;

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
        currentPlayerIndex = 0;
    }

    public TableImpl(List<Player> players, int dealerSeed) {
        pots = new LinkedList<>();
        Pot main = new Pot(0);
        main.setMain(true);
        pots.add(main);
        round = ROUND.BLINDS;
        deck = new Deck();
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
        if (round != ROUND.INTERIM) {
            player.setInRound(false);
        }
        players.add(player);
    }

    @Override
    public void removePlayer(Player player) {
        players.remove(player);
    }

    @Override
    public int getCurrentBet() {
        return this.pots.get(pots.size() - 1).getBet();
    }

    @Override
    public void setCurrentBet(int amt) {
        this.pots.get(pots.size() - 1).setBet(amt);
    }

    @Override
    public Map<Player, Integer> getShowdownWinners() {
        Map<Player, Integer> results = new LinkedHashMap<>();
        for (int i = 0; i < pots.size(); i++) {
            TreeMap<Hand, Player> bestHandMap = new TreeMap<>();
            for (Player p : pots.get(i).getPlayers().stream().filter(Player::isInRound).collect(Collectors.toSet())) {
                bestHandMap.put(p.bestHand(commonCards.toArray(new Card[0])), p);
            }
            if(bestHandMap.isEmpty()) {
                continue;
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
    public void handleBet(Player player, int amount) {
        player.bet(amount);
        addToPot(player,amount);
        setCurrentBet(Math.max(player.getBet(), getCurrentBet()));
    }



    @Override
    public void addToPot(Player p, int amount) {
        int j = 0;
        if (pots.get(0).getBets().isEmpty() || !pots.get(0).getBets().containsKey(p)) {
            pots.get(0).add(p, amount);
            return;
        }
        while (amount > 0 && j < pots.size()) {
            Pot pot = pots.get(j);
            int max = pot.getBets().values().stream().max(Integer::compareTo).orElse(0);
            if(max == 0){
                pot.getBets().replace(p, amount);
                amount = 0;
            } else if (pot.getBets().get(p) < max) {
                int diff = max - pot.getBets().get(p);
                if (amount >= diff) {
                    pot.getBets().replace(p, pot.getBets().get(p) + diff);
                    amount -= diff;
                } else {
                    pot.getBets().replace(p, pot.getBets().get(p) + amount);
                    amount = 0;
                }
            }
            j++;
        }
        pots.get(pots.size() - 1).add(p, amount);
    }

    @Override
    public List<Pot> getPots() {
        return this.pots;
    }

    @Override
    public Pot getLastPot() {
        return getPots().get(getPots().size() - 1);
    }

    @Override
    public void splitLastPot(Player player, int amount) {
        Pot newPot = getLastPot().split(player, amount);
        if (newPot != null) {
            addPot(newPot);
        }
    }

    @Override
    public void addPot(Pot pot) {
        this.pots.add(pot);
    }

    public void setRound(ROUND round) {
        this.round = round;
    }

    @Override
    public boolean hasNext() {
        return !activePlayers().isEmpty();
    }

    @Override
    public boolean hasTwoNext() {
        return activePlayers().size() > 1;
    }

    @Override
    public Player next() {
        if (hasNext()) {
            int i = (currentPlayerIndex + 1) % players.size();
            Player n = players.get(i);
            while (!n.isInRound()) {
                n = players.get((++i) % players.size());
            }
            currentPlayerIndex = i;
            return n;
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
        this.setCurrentBet(0);

        if (this.round.getRoundNum() < 5) {
            this.round = ROUND.getRound(this.round.getRoundNum() + 1);
        } else {
            this.round = ROUND.PREFLOP;
        }

        switch (this.round) {
            case PREFLOP:
                nextRound();
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

    public int getMainPotSize() {
        return pots.get(0).getAmount();
    }

    public int getTotalPotAmount() {
        return pots.stream().map(Pot::getAmount).reduce(Integer::sum).orElse(0);
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

        private final int roundNum;

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
