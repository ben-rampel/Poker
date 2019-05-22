package poker;

import org.springframework.scheduling.annotation.Async;
import webapp.PokerController;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static poker.utils.*;

public class Player {
    /*
     * Player
     * Abstraction of a player in a Poker game
     * Stores a player's game data such as name, hand (hole cards), and number of chips
     */
    protected Card[] hole;
    private final String name;
    private int chips;
    private int requiredBet;
    private TurnNotification myTurn;

    public Player(String name, int startingChips) {
        this.name = name;
        chips = startingChips;
        hole = new Card[2];
    }

    public String getName() {
        return name;
    }

    public void drawHole(Deck d) {
        for (int i = 0; i < 2; i++) {
            hole[i] = d.next();
        }
    }

    public Card[] getHole() {
        return hole;
    }

    public String getHoleAsString() {
        if (hole[0] == null || hole[1] == null) {
            return "";
        } else {
            return hole[0] + ", " + hole[1];
        }
    }

    public int getChips() {
        return chips;
    }

    public void buyIn(int chips) {
        if (this.chips == 0) this.chips = chips;
    }

    public int bestHand(Card[] commons) {
        List<Card> allCards = new ArrayList<>();
        allCards.addAll(Arrays.asList(getHole()));
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
            int pairCount = 0;
            for (Card.Rank r : counts.keySet()) {
                if (counts.get(r) == 2) {
                    pairCount++;
                }
            }
            if (pairCount == 2) {
                if (8 < bestHandValue) bestHandValue = 8;
            }
            //test pair
            if (pair) {
                if (9 < bestHandValue) bestHandValue = 9;
            }

        }
        return bestHandValue;
    }

    public int getRequiredBet() {
        return requiredBet;
    }

    public void setRequiredBet(int requiredBet) {
        this.requiredBet = requiredBet;
    }

    @Async
    public Turn playTurn(TurnNotification t, PokerController controller) throws ExecutionException, InterruptedException {
        Future<Turn> turn = controller.handleTurnNotification(t);
        while(true){
            if(turn.isDone()){
                this.chips -= turn.get().getBetAmount();
                return turn.get();
            } else {
                System.out.println("Continue doing something else. ");
                Thread.sleep(200);
            }
        }
    }

    public void receiveWinnings(int amount){
        chips += amount;
    }

    public void clearHole(){
        hole = new Card[] {null,null};
    }

}
