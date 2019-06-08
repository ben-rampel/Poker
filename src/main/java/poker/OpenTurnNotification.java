package poker;

import java.util.ArrayList;
import java.util.Arrays;

public class OpenTurnNotification extends TurnNotification {
    public OpenTurnNotification(Player player) {
        super(new ArrayList<>(Arrays.asList(Turn.PlayerAction.CHECK, Turn.PlayerAction.BET, Turn.PlayerAction.FOLD)), 0, 0, player);
    }
}
