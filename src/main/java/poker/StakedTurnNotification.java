package poker;

import java.util.ArrayList;
import java.util.Arrays;

public class StakedTurnNotification extends TurnNotification {
    public StakedTurnNotification(Player player, int minimumBet) {
        super(new ArrayList<>(Arrays.asList(Turn.PlayerAction.CALL, Turn.PlayerAction.RAISE, Turn.PlayerAction.FOLD)), minimumBet, 0, player);
    }
}
