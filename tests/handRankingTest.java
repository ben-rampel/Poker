import org.junit.Test;
import poker.Card;
import poker.Hand;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static poker.Card.Rank.*;
import static poker.Card.Suit.*;

public class handRankingTest {
    @Test
    public void rankTest() {
        Hand FH = new Hand(Arrays.asList(
                new Card(seven, hearts), new Card(seven, clubs), new Card(seven, diamonds), new Card(two, clubs), new Card(two, spades)
        ));
        Hand betterFH = new Hand(Arrays.asList(
                new Card(seven, hearts), new Card(seven, clubs), new Card(seven, diamonds), new Card(three, clubs), new Card(three, spades)
        ));
        Hand RF = new Hand(Arrays.asList(
                new Card(ace, hearts), new Card(king, hearts), new Card(queen, hearts), new Card(jack, hearts), new Card(ten, hearts)
        ));
        Hand Flush = new Hand(Arrays.asList(
                new Card(nine, hearts), new Card(three, hearts), new Card(two, hearts), new Card(five, hearts), new Card(six, hearts)
        ));
        Hand NineHC = new Hand(Arrays.asList(
                new Card(nine, clubs), new Card(three, diamonds), new Card(two, hearts), new Card(five, hearts), new Card(six, hearts)
        ));
        Hand twoPairBetter = new Hand(Arrays.asList(
                new Card(nine, clubs), new Card(nine, diamonds), new Card(two, hearts), new Card(two, hearts), new Card(king, hearts)
        ));
        Hand twoPairWorse = new Hand(Arrays.asList(
                new Card(nine, clubs), new Card(nine, diamonds), new Card(two, hearts), new Card(two, hearts), new Card(six, hearts)
        ));

        List<Hand> correctOrder = new LinkedList<>();
        correctOrder.add(NineHC);
        correctOrder.add(twoPairWorse);
        correctOrder.add(twoPairBetter);
        correctOrder.add(Flush);
        correctOrder.add(FH);
        correctOrder.add(betterFH);
        correctOrder.add(RF);

        List<Hand> unordered = new LinkedList<>(Arrays.asList(NineHC, Flush, RF, betterFH, twoPairBetter, FH, twoPairWorse));
        Collections.sort(unordered);
        unordered.forEach(obj -> {
            System.out.println("Hand: " + obj);
            System.out.println("Cards" + obj.getCards());
        });
        assertEquals(unordered, correctOrder);
    }
}
