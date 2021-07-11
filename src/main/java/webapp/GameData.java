package webapp;

import poker.Card;
import poker.Player;
import poker.Pot;
import poker.TurnNotification;

import java.util.Arrays;
import java.util.Map;

public class GameData {
    private int pot;
    private Card[] commonCards;
    private Card[] personalCards;
    private Player[] players;
    private Pot[] sidePots;
    private Player winner;
    private String winnerInfo;

    public int getPot() {
        return pot;
    }

    public void setPot(int pot) {
        this.pot = pot;
    }

    public Pot[] getSidePots() {
        return sidePots;
    }

    public void setSidePots(Pot[] sidePots) {
        this.sidePots = sidePots;
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

    public String getWinnerInfo() {
        return winnerInfo;
    }

    public void setWinnerInfo(String winnerInfo) {
        this.winnerInfo = winnerInfo;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public Map<String, Boolean> getFolded() {
        return folded;
    }

    public void setFolded(Map<String, Boolean> folded) {
        this.folded = folded;
    }

    private Map<String, Boolean> folded;

    private TurnNotification turnNotification;

    @Override
    public String toString() {
        return "GameData{" +
                "pot=" + pot +
                ", commonCards=" + Arrays.toString(commonCards) +
                ", personalCards=" + Arrays.toString(personalCards) +
                ", players=" + Arrays.toString(players) +
                ", sidePots=" + Arrays.toString(sidePots) +
                ", winner=" + winner +
                ", winnerInfo='" + winnerInfo + '\'' +
                ", folded=" + folded +
                ", turnNotification=" + turnNotification +
                '}';
    }
}
