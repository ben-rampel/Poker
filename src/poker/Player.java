package poker;

public class Player {
    private Card[] hole;
    private String name;
    private int chips;

    public Player(String name, int startingChips){
        this.name = name;
        chips = startingChips;
        hole = new Card[2];
    }

    public String getName(){
        return name;
    }

    public void drawHole(Deck d){
        for(int i = 0; i < 2; i++){
            hole[i] = d.next();
        }
    }

    public Card[] getHole() {
        return hole;
    }

    public String getHoleAsString() {
        if(hole[0] == null || hole[1] == null) {
            throw new RuntimeException("No cards in hole");
        } else {
            return hole[0] + ", " + hole[1];
        }
    }

    public int getChips() {
        return chips;
    }

    public void buyIn(int chips){
        if(this.chips == 0) this.chips = chips;
    }


}
