package pokertest;

import org.junit.Test;
import poker.Card;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static poker.utils.*;

public class utilsTests {

    @Test
    public void straightAndFlushTest() {

        List<Card> straight = new ArrayList<>();
        straight.add(new Card(Card.Rank.ace, Card.Suit.hearts));
        straight.add(new Card(Card.Rank.two, Card.Suit.clubs));
        straight.add(new Card(Card.Rank.three, Card.Suit.spades));
        straight.add(new Card(Card.Rank.four, Card.Suit.hearts));
        straight.add(new Card(Card.Rank.five, Card.Suit.hearts));
        assertTrue(isStraight(straight));

        straight.clear();
        straight.add(new Card(Card.Rank.ace, Card.Suit.hearts));
        straight.add(new Card(Card.Rank.ten, Card.Suit.clubs));
        straight.add(new Card(Card.Rank.queen, Card.Suit.spades));
        straight.add(new Card(Card.Rank.jack, Card.Suit.hearts));
        straight.add(new Card(Card.Rank.king, Card.Suit.hearts));

        assertTrue(isStraight(straight));

        List<Card> notStraightIsFlush = new ArrayList<>();
        notStraightIsFlush.add(new Card(Card.Rank.queen, Card.Suit.hearts));
        notStraightIsFlush.add(new Card(Card.Rank.two, Card.Suit.hearts));
        notStraightIsFlush.add(new Card(Card.Rank.three, Card.Suit.hearts));
        notStraightIsFlush.add(new Card(Card.Rank.four, Card.Suit.hearts));
        notStraightIsFlush.add(new Card(Card.Rank.five, Card.Suit.hearts));

        assertTrue(isFlush(notStraightIsFlush));
        assertFalse(isStraight(notStraightIsFlush));

        List<Card> flush = new ArrayList<>();
        flush.add(new Card(Card.Rank.nine, Card.Suit.hearts));
        flush.add(new Card(Card.Rank.queen, Card.Suit.hearts));
        flush.add(new Card(Card.Rank.ten, Card.Suit.hearts));
        flush.add(new Card(Card.Rank.jack, Card.Suit.hearts));
        flush.add(new Card(Card.Rank.eight, Card.Suit.hearts));

        assertTrue(isFlush(flush));

        List<Card> notFlush = new ArrayList<>();
        notFlush.add(new Card(Card.Rank.nine, Card.Suit.clubs));
        notFlush.add(new Card(Card.Rank.queen, Card.Suit.hearts));
        notFlush.add(new Card(Card.Rank.ten, Card.Suit.hearts));
        notFlush.add(new Card(Card.Rank.jack, Card.Suit.diamonds));
        notFlush.add(new Card(Card.Rank.eight, Card.Suit.hearts));

        assertFalse(isFlush(notFlush));


    }

}
