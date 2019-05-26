package webapp;

import poker.Player;

public class BetValueException extends RuntimeException {
    private Player player;

    public BetValueException(Player p){
        player= p;
    }

    public Player getPlayer(){
        return player;
    }
}
