package poker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/*
 * Deck
 * A Deck stores the 52 cards found in a standard deck of cards
 */
public class Deck implements Iterator<Card> {
    final private int index;
    private final List<Card> deck;

    /*
     * Creating a deck object initializes the deck list with a card of each possible rank and suit,
     * making a standard 52-card deck. Then it shuffles itself
     */
    public Deck() {
        deck = new ArrayList<>();
        for (Card.Rank r : Card.Rank.values()) {
            for (Card.Suit s : Card.Suit.values()) {
                deck.add(new Card(r, s));
            }
        }
        index = 0;
        shuffle();
    }

    /*
     * void shuffle()
     * method to pseudorandomly move cards around to simulate shuffling the deck
     */
    public void shuffle() {
        Random r = new Random();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 52; j++) {
                int n = r.nextInt(52);
                Card temp = deck.get(j);
                deck.set(j, deck.get(n));
                deck.set(n, temp);
            }
        }
    }

    /*
     * Card next()
     * draws and returns card from the top of the deck, removing it from the deck's cards
     */
    public Card next() {
        if (hasNext()) {
            Card draw = deck.get(index);
            deck.remove(index);
            return draw;
        } else {
            throw new RuntimeException("Out of cards");
        }
    }

    /*
     * boolean hasNext()
     * returns true if the deck has any card(s) left
     * returns false if the deck is empty
     */
    @Override
    public boolean hasNext() {
        return (deck.size() > 0);
    }

}
