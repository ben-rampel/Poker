package poker;


/*
 * Card
 * Define an abstraction to store and return the information shown on a card
 */
public class Card {
    private final Suit suit;

    private final Rank rank;

    public Card(Suit s, Rank r) {
        suit = s;
        rank = r;
    }
    public Card(Rank r, Suit s) {
        suit = s;
        rank = r;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public boolean equals(Card other) {
        return (rank == other.rank && suit == other.suit);
    }

    public String toString() {
        return rank + " of " + suit;
    }

    public enum Suit {clubs, hearts, diamonds, spades}

    public enum Rank {two, three, four, five, six, seven, eight, nine, ten, jack, queen, king, ace}

}
