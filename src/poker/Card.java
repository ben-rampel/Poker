package poker;


/*
 * Define an abstraction to store a cards information
 */
public class Card {
    public enum Suite {clubs, hearts, diamonds, spades};
    public enum Rank {two,three,four,five,six,seven,eight,nine,ten,jack,queen,king,ace};

    private Suite suite;
    private Rank rank;


    public Card(Suite s, Rank r){
        suite = s;
        rank = r;
    }

    public Card(Rank r, Suite s){
        suite = s;
        rank = r;
    }

    public Suite getSuite() {
        return suite;
    }

    public Rank getRank() {
        return rank;
    }

    public boolean equals(Card other){
        return (rank == other.rank && suite == other.suite);
    }
    public String toString(){
        return rank + " of " + suite;
    }

}
