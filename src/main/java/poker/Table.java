package poker;

import java.util.*;

import static poker.utils.handTypes;

public class Table {
    public enum ROUND {PREFLOP, FLOP, TURN, RIVER, INTERIM}
    private ROUND round;
    private Deck deck;
    //Map the list of playerBets to their current bet in the round
    private Map<Player, Integer> playerBets;
    private Map<Player, Boolean> playersInRound;
    private List<Card> commonCards;
    private int potSize;
    private int dealerIndex;
    private int currentPlayerIndex;

    private TurnNotification currentTurn;

    private final int smallBlind;
    private final int bigBlind;

    private Player winner;

    public ROUND getRound() {
        return round;
    }

    public void setRound(ROUND round) {
        this.round = round;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public Map<Player, Integer> getPlayerBets() {
        return playerBets;
    }

    public void setPlayerBets(Map<Player, Integer> playerBets) {
        this.playerBets = playerBets;
    }

    public Map<Player, Boolean> getPlayersInRound() {
        return playersInRound;
    }

    public void setPlayersInRound(Map<Player, Boolean> playersInRound) {
        this.playersInRound = playersInRound;
    }

    public List<Card> getCommonCards() {
        return commonCards;
    }

    public void setCommonCards(List<Card> commonCards) {
        this.commonCards = commonCards;
    }

    public int getPotSize() {
        return potSize;
    }

    public void setPotSize(int potSize) {
        this.potSize = potSize;
    }

    public int getDealerIndex() {
        return dealerIndex;
    }

    public void setDealerIndex(int dealerIndex) {
        this.dealerIndex = dealerIndex;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public int getSmallBlind() {
        return smallBlind;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public Player getWinner() {
        return winner;
    }

    public void setWinner(Player winner) {
        this.winner = winner;
    }

    public Table(List<Player> players){
        playerBets = new HashMap<>();
        playersInRound = new HashMap<>();
        if(players != null) {
            for (Player player : players) {
                playerBets.put(player, 0);
                playersInRound.put(player,true);
            }
        }
        dealerIndex = 0;
        commonCards = new ArrayList<>();
        potSize = 0;
        round = ROUND.PREFLOP;
        deck = new Deck();
        smallBlind = 1;
        bigBlind = 2;
    }

    public Table(){
        this(null);
    }

    public void addPlayer(Player p){
        playerBets.put(p,0);
        if(round != ROUND.INTERIM){
            playersInRound.put(p,false);
        } else {
            playersInRound.put(p, true);
        }
    }
    /*
     * Start a round of poker by setting dealer, posting small blind, posting big blind, then telling the next player it's their turn
     * Currently assumes both blinds will accept. Needs functionality to give next player chance to post blinds if the first one folds
     *//*
    public void startRound(){
        for(Map.Entry<Player,Boolean> entry: playersInRound.entrySet()){
            entry.setValue(true);
        }
        dealerIndex++;
        Player[] players = playersInRound.keySet().toArray(new Player[0]);
        //Tell player left of dealer he may fold or post the small blind
        Turn lastTurn = new Turn(null,null,0);

       while(players.length > 0) {
            players[(dealerIndex + 1) % players.length].setRequiredBet(smallBlind);
            lastTurn = players[(dealerIndex + 1) % players.length].playTurn(
                    new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CALL),
                            0, smallBlind));
            if(lastTurn.getAction() == Turn.PlayerAction.FOLD){
                playersInRound.put(lastTurn.getPlayer(),false);
                List<Player> nonFoldedPlayers = new ArrayList<>();
                for(Player p : playersInRound.keySet()){
                    if(playersInRound.get(p)){
                        nonFoldedPlayers.add(p);
                    }
                }
                players = nonFoldedPlayers.toArray(new Player[0]);
                currentPlayerIndex++;
            } else {
                break;
            }
        }
        if(players.length == 0) {
            return;
        }
            receivePlayerTurn(lastTurn);
            players[(dealerIndex + 2)%players.length].setRequiredBet(bigBlind);
            lastTurn = players[(dealerIndex + 2)%players.length].playTurn(
                    new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CALL),
                            0, bigBlind));

        for(Player p : players){
            p.drawHole(deck);
            System.out.println(p.getName() +  ", your hand is: " + p.getHoleAsString() + ". You have " + p.getChips() + " chips.");
        }

            receivePlayerTurn(lastTurn);




        System.out.println("POT AMOUNT: " + potSize);

        currentPlayerIndex = dealerIndex + 3;
        lastTurn = players[currentPlayerIndex%players.length].playTurn(
                new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CALL, Turn.PlayerAction.RAISE),
                        playerBets.get(players[(currentPlayerIndex - 1)%players.length]), 0));
        receivePlayerTurn(lastTurn);

        System.out.println("Winner: " + winner.getName() + " with a " + handTypes[winner.bestHand(commonCards.toArray(new Card[0]))-1]);
        winner.receiveWinnings(potSize);

        for(Player p : players){
            p.drawHole(deck);
            System.out.println(p.getName() +  ", your hand was: " + p.getHoleAsString() + ". You now have " + p.getChips() + " chips.");
        }

    }

    private void receivePlayerTurn(Turn t){
        //note: fold needs to remove you from lists and set you to not playing
        //need to check if all players but one have folded
            //if so, end the game
        System.out.println("ROUND: " + round);
        potSize += t.getBetAmount();
        if(t.getBetAmount() > 0) {
            playerBets.put(t.getPlayer(), t.getBetAmount());
        }
        if(t.getAction() == Turn.PlayerAction.FOLD){
            playerBets.remove(t.getPlayer());
            playersInRound.put(t.getPlayer(), false);
        }
        System.out.println("TABLE RECEIVED ACTION OF\n\t " + t.toString());
        List<Player> nonFoldedPlayers = new ArrayList<>();
        for(Player p : playersInRound.keySet()){
            if(playersInRound.get(p)){
                nonFoldedPlayers.add(p);
            }
        }
        Player[] players = nonFoldedPlayers.toArray(new Player[0]);
        //Player[] players = playersInRound.keySet().toArray(new Player[0]);
        currentPlayerIndex++;

        //go to next round once the dealer plays his turn
        if(currentPlayerIndex == dealerIndex+players.length+1){
            switch(round){
                case PREFLOP: round = ROUND.FLOP;
                for(int i = 0; i < 3; i++) {commonCards.add(deck.next());}
                System.out.println("COMMON CARDS: " + commonCards.toString());
                break;
                case FLOP: round = ROUND.TURN;
                commonCards.add(deck.next());
                System.out.println("COMMON CARDS: " + commonCards.toString());
                break;
                case TURN: round = ROUND.RIVER;
                commonCards.add(deck.next());
                System.out.println("COMMON CARDS: " + commonCards.toString());
                break;
                //showdown time -- implement end of game logic
                case RIVER: round = ROUND.INTERIM;
                int bestHand = 10;
                for(int i = 0; i < players.length; i++){
                        if (players[i].bestHand(commonCards.toArray(new Card[0])) < bestHand) {
                            bestHand = players[i].bestHand(commonCards.toArray(new Card[0]));
                            winner = players[i];
                        }
                }
                break;
            }
            if(!(round == ROUND.INTERIM)) {
                playerBets.clear();
                dealerIndex++;
                currentPlayerIndex = dealerIndex + 1;
                receivePlayerTurn(players[currentPlayerIndex % players.length].playTurn(
                        new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CHECK, Turn.PlayerAction.RAISE),
                                0, 0)));
            }
        } else {

            //In the preflop, if we have passed the blinds and players have received their cards
            //and we are still in the same stage (haven't passed dealer), start passing turns around until we get back to the dealer
            if (round == ROUND.PREFLOP && players[currentPlayerIndex % players.length].getHole()[0] != null && currentPlayerIndex > dealerIndex + 3 && currentPlayerIndex <= dealerIndex + players.length) {
                receivePlayerTurn(players[currentPlayerIndex % players.length].playTurn(
                        new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CALL, Turn.PlayerAction.RAISE),
                                playerBets.get(players[(currentPlayerIndex - 1) % players.length]), 0)));

            }
            if (!(round == ROUND.PREFLOP || round == ROUND.INTERIM)) {
                if (!playerBets.containsKey(players[(currentPlayerIndex - 1) % players.length]) || playerBets.get(players[(currentPlayerIndex - 1) % players.length]) == 0) {
                    receivePlayerTurn(players[currentPlayerIndex % players.length].playTurn(
                            new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CHECK, Turn.PlayerAction.RAISE),
                                    0, 0)));
                } else {
                    receivePlayerTurn(players[currentPlayerIndex % players.length].playTurn(
                            new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CALL, Turn.PlayerAction.RAISE),
                                    playerBets.get(players[(currentPlayerIndex - 1) % players.length]), 0)));
                }
            }
        }


    }
*/
    public Player getPlayerFromName(String name){
        for(Player p : playersInRound.keySet()){
            if(name.equals(p.getName()) || name.equals(p.getName().toLowerCase())){
                return p;
            }
        }
        return null;
    }

}
