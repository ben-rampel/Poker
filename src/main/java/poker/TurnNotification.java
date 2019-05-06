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

    public Player getPlayer(){
        return player;
    }
}
