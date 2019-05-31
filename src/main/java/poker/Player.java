package poker;

import webapp.BetValueException;
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

    public Hand bestHand(Card[] commons) {
        List<Card> allCards = new ArrayList<>();
        allCards.addAll(Arrays.asList(getHole()));
        allCards.addAll(Arrays.asList(commons));
        List<Hand> hands = getAllHands(allCards);
        Collections.sort(hands);
        return hands.get(hands.size()-1);
    }

    public Turn playTurn(TurnNotification t, PokerController controller) throws ExecutionException, InterruptedException {
        Future<Turn> turn = controller.handleTurnNotification(t);
        while(true){
            if(turn.isDone()){
                if(t.getPlayer().getChips() - turn.get().getBetAmount() < 0){
                    System.out.print(t.getPlayer().getChips() + " " +  turn.get().getBetAmount());
                    throw new BetValueException(this);
                }
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
