package pokertest;

import poker.Card;
import poker.Player;
import poker.Turn;
import webapp.Lobby;

public class TestPlayer extends Player {
    public Lobby lobby;

    TestPlayer(String name, int chips, Lobby lobby) {
        super(name, chips);
        this.lobby = lobby;
    }

    TestPlayer() {
        super("test", 255);
    }

    void setHand(Card[] hand) {
        this.hole = hand;
    }

    void call(int amt){
        this.lobby.receiveTurn(new Turn(this, Turn.PlayerAction.CALL, amt));
    }

    void sendBet(int amt){
        this.lobby.receiveTurn(new Turn(this, Turn.PlayerAction.BET, amt));
    }

    void raise(int amt){
        this.lobby.receiveTurn(new Turn(this, Turn.PlayerAction.RAISE, amt));
    }

    void check(){
        this.lobby.receiveTurn(new Turn(this, Turn.PlayerAction.CHECK, 0));
    }

    void allin(){
        this.lobby.receiveTurn(new Turn(this, Turn.PlayerAction.ALLIN, 0));
    }

    void fold(){
        this.lobby.receiveTurn(new Turn(this, Turn.PlayerAction.FOLD, 0));
    }

}
