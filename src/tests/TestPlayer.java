package tests;

import poker.Card;
import poker.Player;

public class TestPlayer extends Player {
    public TestPlayer(){
        super("test",250);
    }
     public void setHand(Card[] hand){
        this.hole = hand;
    }
}
