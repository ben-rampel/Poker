package poker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Pot {
    private boolean isMain;
    private int bet;
    private Map<Player, Integer> bets;

    public Pot(int bet) {
        this.bet = bet;
        this.isMain = false;
        this.bets = new HashMap<>();
    }

    public void add(Player player, int amount) {
        if (bets.containsKey(player)) {
            int total = bets.get(player) + amount;
            if (player.getBet() > bet) bet = player.getBet();
            bets.replace(player, total);
        } else {
            bets.put(player, amount);
            if (player.getBet() > bet) bet = player.getBet();
        }
    }

    public Pot split(Player player, int amount) {
        Pot sidePot = new Pot(bet - amount);
        this.add(player, amount);
        for (Player p : bets.keySet()) {
            if (p != player) {
                sidePot.getBets().put(p, bet - amount);
                this.bets.replace(p, bets.get(p) - (bet - amount));
            }
        }
        this.bet = amount;
        return sidePot;
    }

    public List<Player> getPlayers() {
        return new LinkedList<>(bets.keySet());
    }

    public int getAmount() {
        return bets.values().stream().reduce(Integer::sum).orElse(0);
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }

    public Map<Player, Integer> getBets() {
        return bets;
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int x) {
        this.bet = x;
    }

    public void setBets(Map<Player, Integer> bets) {
        this.bets = bets;
    }

    @Override
    public String toString() {
        return "Pot{" +
                "isMain=" + isMain +
                ", bet=" + bet +
                ", bets=" + bets +
                '}';
    }
}

