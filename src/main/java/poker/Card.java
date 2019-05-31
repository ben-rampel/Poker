package poker;


/*
 * Card
 * Define an abstraction to store and return the information shown on a card
 */
public class Card implements Comparable<Card>{
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

    public enum Rank {
        aceLow(0),two(1),three(2),four(3),five(4),six(5),seven(6),eight(7),nine(8),ten(9),jack(10),queen(11),king(12),ace(13);
        private final int num;
        Rank(int n){
            this.num = n;
        }
        public int index(){
            return this.num;
        }
    }

    public int compareTo(Card other){
        return this.rank.index() - other.rank.index();
    }
}
