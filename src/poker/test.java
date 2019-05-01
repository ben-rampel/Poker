package poker;

import java.util.*;

public class test {
    public static void main(String[] args) {

        /* Test Deck Shuffle */
        Deck d = new Deck();

        Player[] players = new Player[4];
        String[] names = {"Bob", "Joe", "John", "Jim"};

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

        System.out.println("\n Best Hands");
        for(Player p : players){
            System.out.println(p.getName() + " " + handTypes[bestHand(p,commons)]);
        }

        //test straight detection

       /* List<Card> straight = new ArrayList<>();
        straight.add(new Card(Card.Rank.nine, Card.Suite.hearts));
        straight.add(new Card(Card.Rank.queen, Card.Suite.clubs));
        straight.add(new Card(Card.Rank.ten, Card.Suite.spades));
        straight.add(new Card(Card.Rank.jack, Card.Suite.hearts));
        straight.add(new Card(Card.Rank.eight, Card.Suite.hearts));

        for(Card c : straight){
            System.out.println(c);
        }
        System.out.println("Straight?: " + isStraight(straight));*/

        //test flush detection
       /* List<Card> flush = new ArrayList<>();
        flush.add(new Card(Card.Rank.nine, Card.Suite.hearts));
        flush.add(new Card(Card.Rank.queen, Card.Suite.hearts));
        flush.add(new Card(Card.Rank.ten, Card.Suite.hearts));
        flush.add(new Card(Card.Rank.jack, Card.Suite.hearts));
        flush.add(new Card(Card.Rank.eight, Card.Suite.diamonds));

        for(Card c : flush){
            System.out.println(c);
        }
        System.out.println("Flush?: " + isFlush(flush));*/


    }
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
    public static String[] handTypes = {"","Royal Flush","Straight Flush", "Four of a kind", "Full House", "Flush", "Straight","Three of a kind","Two pairs","Pair","High card"};

    public static int bestHand(Player player, Card[] commons){

        List<Card> allCards = new ArrayList<>();
        allCards.addAll(Arrays.asList(player.getHole()));
        allCards.addAll(Arrays.asList(commons));

        List<List<Card>> hands = getAllHands(allCards);
        int bestHandValue = 10;
        for(List<Card> hand : hands ) {
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
            for (Card.Suite s : Card.Suite.values()) {
                if (containsCard(hand, Card.Rank.ace, s) && containsCard(hand, Card.Rank.king, s) && containsCard(hand, Card.Rank.queen, s) && containsCard(hand, Card.Rank.jack, s) && containsCard(hand, Card.Rank.ten, s)) {
                    return 1;
                }
            }
            //Test straight flush
            if (isStraight(hand) && isFlush(hand)) {
                if(2 < bestHandValue) bestHandValue = 2;
            }
            //test 4 of a kind
            for (Card.Rank r : counts.keySet()) {
                if (counts.get(r) >= 4) {
                    if(3 < bestHandValue) bestHandValue = 3;
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
                if(4 < bestHandValue) bestHandValue = 4;
            }
            //test flush
            if (isFlush(hand)) {
                if(5 < bestHandValue) bestHandValue = 5;
            }
            //test straight
            if (isStraight(hand)) {
                if(6 < bestHandValue) bestHandValue = 6;
            }
            //test trips
            if (trips) {
                if(7 < bestHandValue) bestHandValue = 7;
            }
            //test 2 pair
            int paircount = 0;
            for (Card.Rank r : counts.keySet()) {
                if (counts.get(r) == 2) {
                    paircount++;
                }
            }
            if (paircount == 2) {
                if(8 < bestHandValue) bestHandValue = 8;
            }
            //test pair
            if (pair) {
                if(9 < bestHandValue) bestHandValue = 9;
            }

        }
        return bestHandValue;
    }
    public static boolean containsCard(List<Card> cards, Card.Rank rank, Card.Suite suite){
        Card testCard = new Card(rank,suite);
        for(Card c : cards){
            if(c.equals(testCard)){
                return true;
            }
        }
        return false;
    }

    public static boolean isStraight(List<Card> cards){
        List<Card> sorted = new ArrayList<>(cards);
        RankOrderComparator comp = new RankOrderComparator();
        sorted.sort(comp);
        for(int i = 0; i < sorted.size() - 1; i++){
            if(comp.compare(sorted.get(i),sorted.get(i+1)) != -1){
                return false;
            }
        }
        return true;
    }

    public static boolean isFlush(List<Card> cards){
        boolean allHearts = true, allSpades = true, allDiamonds = true, allClubs = true;
        for(Card c : cards){
            if(c.getSuite() != Card.Suite.hearts) allHearts = false;
            if(c.getSuite() != Card.Suite.spades) allSpades = false;
            if(c.getSuite() != Card.Suite.diamonds) allDiamonds = false;
            if(c.getSuite() != Card.Suite.clubs) allClubs = false;
        }
         return (allSpades || allDiamonds || allHearts || allClubs);
    }

    public static List<List<Card>> powerSet(Card[] set){
        String[] mask = new String[((int) Math.pow(2,set.length))];

        for(int i = 0; i < mask.length; i++){
            String binary = Integer.toBinaryString(i);
            while(binary.length() < set.length){
                binary = "0".concat(binary);
            }
            mask[i] = binary;
        }

        List<List<Card>> powerSet = new ArrayList<>();

        for(int i = 0; i < mask.length; i++){
            List<Card> subset = new ArrayList<>();
            for(int j = 0; j < set.length; j++){
                if(mask[i].toCharArray()[j]  == '1'){
                    subset.add(set[j]);
                }
            }
            powerSet.add(subset);
        }
        return powerSet;
    }

    public static List<List<Card>> getAllHands(List<Card> hand){
        List<List<Card>> handPowerSet = powerSet(hand.toArray(new Card[0]));
        List<List<Card>> fiveCardHands = new ArrayList<>();

        for(List<Card> set : handPowerSet){
            if(set.size() == 5){
                fiveCardHands.add(set);
            }
        }

        return fiveCardHands;
    }
}
