package poker;

import other_stuff.SetOperations;
import poker.handComparators.RankOrderComparator;
import poker.handComparators.RankOrderComparatorAceLow;

import java.util.*;

public class utils {
    //Hold 'em hands in best to worst order
    public static final String[] handTypes = {
            "Royal Flush",
            "Straight Flush",
            "Four of a kind",
            "Full House",
            "Flush",
            "Straight",
            "Three of a kind",
            "Two pairs",
            "Pair",
            "High card"
    };

    /*
     * boolean containsCard(List<Card>,rank,suite)
     * returns true if the list contains the card with specified rank and suit
     * returns false if list does not contain such card
     */
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
        //sort hand from lowest card to highest
        List<Card> sortedAceHigh = new ArrayList<>(cards);
        RankOrderComparator comp = new RankOrderComparator();
        sortedAceHigh.sort(comp);
        boolean straight = true;
        //check for each card if the next card is directly after in the rank orders
        for (int i = 0; i < sortedAceHigh.size() - 1; i++) {
            //noinspection ComparatorResultComparison
            if ((comp.compare(sortedAceHigh.get(i), sortedAceHigh.get(i + 1)) != -1)) { //check if cards are connected
                straight = false;
            }
        }
        if (straight) {
            return true;
        } else {
            //test if the cards are straight with ace low
            straight = true;
            List<Card> sortedAceLow = new ArrayList<>(cards);
            comp = new RankOrderComparatorAceLow();
            sortedAceLow.sort(comp);
            //check for each card if the next card is directly after in the rank orders
            for (int i = 0; i < sortedAceLow.size() - 1; i++) {
                //noinspection ComparatorResultComparison
                if ((comp.compare(sortedAceLow.get(i), sortedAceLow.get(i + 1)) != -1)) { //check if cards are connected
                    straight = false;
                }
            }
            return straight;
        }
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
    public static List<List<Card>> getAllHands(List<Card> cards) {
        List<List<Card>> fiveCardHands = new ArrayList<>(); //List to be returned

        //Get power set of cards, loop through all its subsets, add ones of length 5 to fiveCardHands
        for (List<Card> set : SetOperations.powerSet(cards.toArray(new Card[0]))) {
            if (set.size() == 5) {
                fiveCardHands.add(set);
            }
        }

        return fiveCardHands;
    }
}
