package pokertest;

import poker.Card;
import poker.Player;

public class TestPlayer extends Player {
    TestPlayer() {
        super("test", 250);
    }

    void setHand(Card[] hand) {
        this.hole = hand;
    }
}
