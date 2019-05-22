package webapp;

import com.sun.org.apache.xpath.internal.operations.Bool;
import poker.Card;
import poker.Player;
import poker.TurnNotification;

import java.util.Map;

public class GameData {
    public int getPot() {
        return pot;
    }

    public void setPot(int pot) {
        this.pot = pot;
    }

    public Card[] getCommonCards() {
        return commonCards;
    }

    public void setCommonCards(Card[] commonCards) {
        this.commonCards = commonCards;
    }

    public Card[] getPersonalCards() {
        return personalCards;
    }

    public void setPersonalCards(Card[] personalCards) {
        this.personalCards = personalCards;
    }

    public Player[] getPlayers() {
        return players;
    }

    public void setPlayers(Player[] players) {
        this.players = players;
    }

    public TurnNotification getTurnNotification() {
        return turnNotification;
    }

    public void setTurnNotification(TurnNotification turnNotification) {
        this.turnNotification = turnNotification;
    }

    private int pot;
    private Card[] commonCards;
    private Card[] personalCards;
    private Player[] players;

    public String getWinnerInfo() {
        return winnerInfo;
    }

    public void setWinnerInfo(String winnerInfo) {
        this.winnerInfo = winnerInfo;
    }

    private String winnerInfo;

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    private Player winner;


    public Map<String, Boolean> getFolded() {
        return folded;
    }

    public void setFolded(Map<String, Boolean> folded) {
        this.folded = folded;
    }

    private Map<String, Boolean> folded;

    private TurnNotification turnNotification;

}
