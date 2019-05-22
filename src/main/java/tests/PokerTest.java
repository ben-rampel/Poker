package tests;

import poker.Player;
import poker.Table;

public class PokerTest {
    public static void main(String[] args){
        Table test = new Table();
        test.addPlayer(new Player("John", 250));
        test.addPlayer(new Player("Bob", 250));
        test.addPlayer(new Player("Carl", 250));
        test.addPlayer(new Player("Joe", 250));
        //test.startRound();

    }
}
