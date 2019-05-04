package tests;

import poker.Card;
import poker.Deck;
import poker.Player;

import static poker.utils.handTypes;

/*
 * Demonstrates drawing from a Deck to fill 7 player's hands and 5 common cards
 * Also displays best hand method
 */
public class deckTest {
    public static void main(String[] args) {
        Deck d = new Deck();

        Player[] players = new Player[7];
        String[] names = {"Bob", "Joe", "John", "Jim", "Jack", "Jacob", "Ben", "Chris"};
        System.out.println("Player Hole Cards: ");
        for (int i = 0; i < players.length; i++) {
            players[i] = new Player(names[i], 250);
            players[i].drawHole(d);
            System.out.println(players[i].getName() + ": " + players[i].getHoleAsString());
        }


        System.out.println("\nCommon Cards: ");
        Card[] commons = new Card[5];
        for (int i = 0; i < 5; i++) {
            commons[i] = d.next();
        }
        for (int i = 0; i < 5; i++) {
            if (i < 4) {
                System.out.print(commons[i].toString() + ", ");
            } else {
                System.out.print(commons[i].toString());
            }
        }
        System.out.println();
        System.out.println("\nBest Hands: ");
        for (Player p : players) {
            System.out.println(p.getName() + " " + handTypes[p.bestHand(commons) - 1]);
        }
    }
}
/* Sample Output */
/*
Player Hole Cards:
Bob: eight of spades, five of hearts
Joe: ten of spades, king of clubs
John: nine of diamonds, four of spades
Jim: five of spades, seven of clubs
Jack: queen of hearts, eight of diamonds
Jacob: three of spades, jack of diamonds
Ben: ace of spades, jack of hearts

Common Cards:
five of diamonds, two of hearts, ten of diamonds, six of spades, ace of clubs

Best Hands:
Bob Pair
Joe Pair
John High card
Jim Pair
Jack High card
Jacob High card
Ben Pair
 */