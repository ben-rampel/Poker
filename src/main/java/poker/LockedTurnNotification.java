package poker;

import java.util.ArrayList;
import java.util.Arrays;

public class LockedTurnNotification extends TurnNotification {
    public LockedTurnNotification(Player player, int requiredBet) {
        super(new ArrayList<>(Arrays.asList(Turn.PlayerAction.CALL, Turn.PlayerAction.FOLD)), 0, requiredBet, player);
    }
}