package poker;


import java.util.ArrayList;
import java.util.Arrays;

import static poker.Turn.PlayerAction.CALL;
import static poker.Turn.PlayerAction.FOLD;

public class SmallBlindNotification extends TurnNotification {
    public SmallBlindNotification(Player player) {
        super(new ArrayList<>(Arrays.asList(FOLD, CALL)), 0, utils.smallBlind, player);
    }
}
