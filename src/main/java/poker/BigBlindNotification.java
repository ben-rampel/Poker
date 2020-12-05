package poker;


import java.util.ArrayList;
import java.util.Arrays;

import static poker.Turn.PlayerAction.CALL;
import static poker.Turn.PlayerAction.FOLD;

public class BigBlindNotification extends TurnNotification {
    public BigBlindNotification(Player player) {
        super(new ArrayList<>(Arrays.asList(FOLD, CALL)), 0, utils.bigBlind, player);
    }
}
