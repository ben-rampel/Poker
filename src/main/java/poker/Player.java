package poker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static poker.utils.getAllHands;

/*
 * Player
 * Abstraction of a player in a Poker game
 * Stores a player's game data such as name, hand (hole cards), and number of chips
 */
public class Player {

    protected Card[] hole;
    private final String name;
    private int chips;
    private int bet;
    private boolean inRound;
    private boolean isDealer;
    private boolean allIn = false;
    private String websocketsSession;

    public Player(String name, int startingChips) {
        this.name = name;
        chips = startingChips;
        hole = new Card[2];
        inRound = true;
    }

    public void bet(int amt) {
        if(amt == chips){
            setAllIn(true);
        }
        if (amt <= chips) {
            chips -= amt;
            bet += amt;
        } else {
            throw new IllegalStateException("player bet more than he has");
        }
    }

    public boolean isDealer() {
        return isDealer;
    }

    public void setDealer(boolean dealer) {
        isDealer = dealer;
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public boolean isInRound() {
        return inRound;
    }

    public void setInRound(boolean inRound) {
        this.inRound = inRound;
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

    public int getChips() {
        return chips;
    }

    public void buyIn(int chips) {
        if (this.chips == 0) this.chips = chips;
    }

    public Hand bestHand(Card[] commons) {
        List<Card> allCards = new ArrayList<>();
        allCards.addAll(Arrays.asList(getHole()));
        allCards.addAll(Arrays.asList(commons));
        List<Hand> hands = getAllHands(allCards);
        Collections.sort(hands);
        return hands.get(hands.size() - 1);
    }

    @Override
    public String toString() {
        return "Player{" +
                "hole=" + Arrays.toString(hole) +
                ", name='" + name + '\'' +
                ", chips=" + chips +
                ", bet=" + bet +
                ", inRound=" + inRound +
                '}';
    }

    public void receiveWinnings(int amount) {
        chips += amount;
    }

    public void clearHole() {
        hole = new Card[]{null, null};
    }

    public String getWebsocketsSession() {
        return websocketsSession;
    }

    public void setWebsocketsSession(String websocketsSession) {
        this.websocketsSession = websocketsSession;
    }

    public boolean isAllIn() {
        return allIn;
    }

    public void setAllIn(boolean allIn) {
        this.allIn = allIn;
    }
}
