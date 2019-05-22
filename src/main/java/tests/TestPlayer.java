package tests;

import poker.Card;
import poker.Player;

class TestPlayer extends Player {
    TestPlayer(){
        super("test",250);
    }
    void setHand(Card[] hand){
        this.hole = hand;
    }
}
