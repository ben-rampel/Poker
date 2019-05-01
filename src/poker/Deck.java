package poker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/*
 * A deck is created storing the 52 different cards in a standard deck of cards and shuffled
 * One card at a time can be drawn from the top with the next() method, removing said card
 * from the deck.
 */
public class Deck implements Iterator<Card> {
    private List<Card> deck;
    final private int index;

    public Deck(){
        deck = new ArrayList<>();
        for(Card.Rank r : Card.Rank.values()) {
            for(Card.Suite s : Card.Suite.values()){
                deck.add(new Card(r,s));
            }
        }
        index = 0;
        shuffle();
    }

    public void shuffle(){
        Random r = new Random();
        for(int i = 0; i < 10; i++) {
            for (int j = 0; j < 52; j++) {
                int n = r.nextInt(52);
                Card temp = deck.get(j);
                deck.set(j, deck.get(n));
                deck.set(n, temp);
            }
        }
    }

    public Card next(){
        if(hasNext()) {
            Card draw = deck.get(index);
            deck.remove(index);
            return draw;
        } else {
            throw new RuntimeException("Out of cards");
        }
    }

    @Override
    public boolean hasNext() {
        return (deck.size() > 0);
    }

}
