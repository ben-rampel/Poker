package poker;

import other_stuff.SetOperations;

import java.util.*;

public class utils {
    /*
    Hand Rankings
      1 royal flush
      2 straight flush
      3 quads
      4 full house
      5 flush
      6 straight
      7 trips
      8 2-pair
      9 pair
      10 high card
     */
    public static String[] handTypes = {"Royal Flush", "Straight Flush", "Four of a kind", "Full House", "Flush", "Straight", "Three of a kind", "Two pairs", "Pair", "High card"};


    //tests for utils methods
    public static void main(String[] args) {

        /* Test Deck Shuffle */
/*        Deck d = new Deck();

        Player[] players = new Player[7];
        String[] names = {"Bob", "Joe", "John", "Jim","Jack","Jacob","Ben","Chris"};

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

        System.out.println("\nBest Hands: ");
        for(Player p : players){
            System.out.println(p.getName() + " " + handTypes[bestHand(p,commons)-1]);
        }*/

        //test straight detection

        List<Card> straight = new ArrayList<>();
        straight.add(new Card(Card.Rank.ace, Card.Suit.hearts));
        straight.add(new Card(Card.Rank.two, Card.Suit.clubs));
        straight.add(new Card(Card.Rank.three, Card.Suit.spades));
        straight.add(new Card(Card.Rank.four, Card.Suit.hearts));
        straight.add(new Card(Card.Rank.five, Card.Suit.hearts));

        System.out.println("is A,2,3,4,5 straight?: " + isStraight(straight));

        straight.clear();
        straight.add(new Card(Card.Rank.ace, Card.Suit.hearts));
        straight.add(new Card(Card.Rank.ten, Card.Suit.clubs));
        straight.add(new Card(Card.Rank.queen, Card.Suit.spades));
        straight.add(new Card(Card.Rank.jack, Card.Suit.hearts));
        straight.add(new Card(Card.Rank.king, Card.Suit.hearts));

        System.out.println("is T,J,Q,K,A straight?: " + isStraight(straight));

        //test flush detection
       /* List<Card> flush = new ArrayList<>();
        flush.add(new Card(Card.Rank.nine, Card.Suit.hearts));
        flush.add(new Card(Card.Rank.queen, Card.Suit.hearts));
        flush.add(new Card(Card.Rank.ten, Card.Suit.hearts));
        flush.add(new Card(Card.Rank.jack, Card.Suit.hearts));
        flush.add(new Card(Card.Rank.eight, Card.Suit.diamonds));

        for(Card c : flush){
            System.out.println(c);
        }
        System.out.println("Flush?: " + isFlush(flush));*/


    }

/*
 * methods that will be used in the poker game
 * most will be moved to their respective classes as instance methods
 */

    public static int bestHand(Player player, Card[] commons) {

        List<Card> allCards = new ArrayList<>();
        allCards.addAll(Arrays.asList(player.getHole()));
        allCards.addAll(Arrays.asList(commons));

        List<List<Card>> hands = getAllHands(allCards);
        int bestHandValue = 10;
        for (List<Card> hand : hands) {
            //Create map of the number of each rank in the hand
            Map<Card.Rank, Integer> counts = new HashMap<>();
            for (Card.Rank r : Card.Rank.values()) {
                for (Card c : hand) {
                    if (c.getRank() == r) {
                        if (counts.containsKey(r)) {
                            counts.put(r, counts.get(r) + 1);
                        } else {
                            counts.put(r, 1);
                        }
                    }
                }
            }
            //Test royal flush
            for (Card.Suit s : Card.Suit.values()) {
                if (containsCard(hand, Card.Rank.ace, s) && containsCard(hand, Card.Rank.king, s) && containsCard(hand, Card.Rank.queen, s) && containsCard(hand, Card.Rank.jack, s) && containsCard(hand, Card.Rank.ten, s)) {
                    return 1;
                }
            }
            //Test straight flush
            if (isStraight(hand) && isFlush(hand)) {
                if (2 < bestHandValue) bestHandValue = 2;
            }
            //test 4 of a kind
            for (Card.Rank r : counts.keySet()) {
                if (counts.get(r) >= 4) {
                    if (3 < bestHandValue) bestHandValue = 3;
                }
            }
            //test full house
            boolean trips = false, pair = false;
            for (Card.Rank r : counts.keySet()) {
                if (counts.get(r) == 3) {
                    trips = true;
                }
                if (counts.get(r) == 2) {
                    pair = true;
                }
            }
            if (trips && pair) {
                if (4 < bestHandValue) bestHandValue = 4;
            }
            //test flush
            if (isFlush(hand)) {
                if (5 < bestHandValue) bestHandValue = 5;
            }
            //test straight
            if (isStraight(hand)) {
                if (6 < bestHandValue) bestHandValue = 6;
            }
            //test trips
            if (trips) {
                if (7 < bestHandValue) bestHandValue = 7;
            }
            //test 2 pair
            int paircount = 0;
            for (Card.Rank r : counts.keySet()) {
                if (counts.get(r) == 2) {
                    paircount++;
                }
            }
            if (paircount == 2) {
                if (8 < bestHandValue) bestHandValue = 8;
            }
            //test pair
            if (pair) {
                if (9 < bestHandValue) bestHandValue = 9;
            }

        }
        return bestHandValue;
    }

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
