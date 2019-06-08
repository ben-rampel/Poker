package poker;

import other_stuff.SetOperations;

import java.util.*;

import static poker.Card.Rank.aceLow;

public class utils {
    /*
     * boolean containsCard(List<Card>,rank,suite)
     * returns true if the list contains the card with specified rank and suit
     * returns false if list does not contain such card
     */

    public static int smallBlind = 1;
    public static int bigBlind = 2;

    public static boolean containsCard(List<Card> cards, Card.Rank rank, Card.Suit suit) {
        Card testCard = new Card(rank, suit);
        for (Card c : cards) {
            if (c.equals(testCard)) {
                return true;
            }
        }
        return false;
    }

    /*
     * boolean isStraight(List<Card>)
     * returns true if a 5 card hand counts as a straight in hold 'em
     * returns false if the hand is not a straight
     */
    public static boolean isStraight(List<Card> cards) {
        if (cards.size() != 5) {
            throw new IllegalStateException();
        }

        List<Card> clone = new LinkedList<>(cards);
        Collections.sort(clone);

        boolean straight = true;
        for (int i = 0; i < clone.size() - 1; i++) {
            if (clone.get(i).compareTo(clone.get(i + 1)) != -1) straight = false;
        }

        if (!straight && clone.get(4).getRank() == Card.Rank.ace) {
            clone.set(4, new Card(clone.get(4).getSuit(), aceLow));
            straight = isStraight(clone);
        }

        return straight;
    }

    /*
     * boolean isFlush(List<Card>)
     * returns true if all cards are the of the same suit (hearts, spades, diamonds, or clubs)
     * returns false if the hand does not count as a flush
     */
    public static boolean isFlush(List<Card> cards) {
        boolean allHearts = true, allSpades = true, allDiamonds = true, allClubs = true;
        for (Card c : cards) {
            if (c.getSuit() != Card.Suit.hearts) allHearts = false;
            if (c.getSuit() != Card.Suit.spades) allSpades = false;
            if (c.getSuit() != Card.Suit.diamonds) allDiamonds = false;
            if (c.getSuit() != Card.Suit.clubs) allClubs = false;
        }
        return (allSpades || allDiamonds || allHearts || allClubs);
    }

    /*
     * List<List<Card>> getAllHands(List<Card>)
     * By obtaining the power set (set of all possible subsets) of the card list and filtering to subsets of cardinality 5,
     * this method returns a list of all possible 5-card hands from a list of cards.
     */
    public static List<Hand> getAllHands(List<Card> cards) {
        List<Hand> fiveCardHands = new ArrayList<>(); //List to be returned

        //Get power set of cards, loop through all its subsets, add ones of length 5 to fiveCardHands
        for (List<Card> set : SetOperations.powerSet(cards.toArray(new Card[0]))) {
            if (set.size() == 5) {
                fiveCardHands.add(new Hand(set));
            }
        }

        return fiveCardHands;
    }
}
