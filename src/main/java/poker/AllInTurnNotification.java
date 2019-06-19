package poker;

import java.util.ArrayList;
import java.util.Arrays;

public class AllInTurnNotification extends TurnNotification {
    public AllInTurnNotification(Player player) {
        super(new ArrayList<>(Arrays.asList(Turn.PlayerAction.ALLIN, Turn.PlayerAction.FOLD)), 0, player.getChips(), player);
    }
}
