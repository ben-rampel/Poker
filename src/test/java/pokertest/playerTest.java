package pokertest;

import org.junit.Test;
import poker.Card;
import poker.Hand;
import poker.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class playerTest {
    @Test
    public void test() {
        List<Player> playerList = new LinkedList<>();
        Card[] commons = {
                new Card(Card.Rank.ace, Card.Suit.spades),
                new Card(Card.Rank.two, Card.Suit.spades),
                new Card(Card.Rank.four, Card.Suit.diamonds),
                new Card(Card.Rank.nine, Card.Suit.clubs),
                new Card(Card.Rank.queen, Card.Suit.spades)
        };
        TestPlayer twoPair = new TestPlayer();
        twoPair.setHand(new Card[]{new Card(Card.Rank.ace, Card.Suit.hearts), new Card(Card.Rank.two, Card.Suit.hearts)});
        playerList.add(twoPair);
        TestPlayer flush = new TestPlayer();
        flush.setHand(new Card[]{new Card(Card.Rank.five, Card.Suit.spades), new Card(Card.Rank.six, Card.Suit.spades)});
        playerList.add(flush);

        TreeMap<Hand, Player> bestHandMap = new TreeMap<>();
        for (Player p : playerList) {
            bestHandMap.put(p.bestHand(commons), p);
        }
        System.out.println(twoPair.bestHand(commons) + " " + flush.bestHand(commons));
        System.out.println(twoPair.bestHand(commons).compareTo(flush.bestHand(commons)));

        assertEquals(bestHandMap.lastEntry().getValue(), flush);
    }
}
