package tests;

import org.junit.Test;
import poker.Card;

import static org.junit.Assert.assertEquals;

public class playerTests {
    @Test
    public void test(){
        Card[] commons = {
                new Card(Card.Rank.ace, Card.Suit.spades),
                new Card(Card.Rank.two, Card.Suit.spades),
                new Card(Card.Rank.four, Card.Suit.diamonds),
                new Card(Card.Rank.nine, Card.Suit.clubs),
                new Card(Card.Rank.queen, Card.Suit.spades)
        };
       TestPlayer twoPair = new TestPlayer();
       twoPair.setHand(new Card[] {new Card(Card.Rank.ace, Card.Suit.hearts), new Card(Card.Rank.two, Card.Suit.hearts)});
       TestPlayer flush = new TestPlayer();
       flush.setHand(new Card[] {new Card(Card.Rank.five, Card.Suit.spades), new Card(Card.Rank.six, Card.Suit.spades)});

       assertEquals(twoPair.bestHand(commons),8);
       assertEquals(flush.bestHand(commons),5);
    }
}
