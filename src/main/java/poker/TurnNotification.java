package poker;

import java.util.List;

/*
 * Notifies a player that it is his turn, encapsulating the information needed to specify his possible turn options
 */
public class TurnNotification {
    private List<Turn.PlayerAction> options;
    private int minimumBet;
    private int requiredBet;
    private Player player;

    public TurnNotification(List<Turn.PlayerAction> options, int minimumBet, int requiredBet, Player player) {
        this.options = options;
        this.minimumBet = minimumBet;
        this.player = player;
        this.requiredBet = requiredBet;
    }

    public List<Turn.PlayerAction> getOptions() {
        return options;
    }

    public int getMinimumBet() {
        return minimumBet;
    }

    public int getRequiredBet() {
        return requiredBet;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(player.getName() + " may ");
        for (int i = 0; i < options.size(); i++) {
            Turn.PlayerAction a = options.get(i);
            builder.append(a);
            switch (a) {
                case RAISE:
                case CALL:
                    builder.append(" ").append((minimumBet > 0) ? minimumBet : requiredBet);
            }
            if (i < options.size() - 1) builder.append(", ");
        }
        return builder.toString();
    }
}
