package webapp;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import poker.*;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static poker.Turn.PlayerAction.CALL;
import static poker.utils.handTypes;

public class PokerController {
    private Table table;
    private Turn currentTurn;
    private TurnNotification currentTurnNotification;

    public String winnerInfo;

    @Async
    public String startGame() {
        System.out.println("startgame");
        table = new Table();
        List<Player> playerList = new LinkedList<>();
        playerList.add(new Player("John", 250));
        playerList.add(new Player("Bob", 250));
        playerList.add(new Player("Carl", 250));
        playerList.add(new Player("Joe", 250));
        addPlayers(playerList);
        try {
            startRound();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void addPlayers(List<Player> players) {
        for (Player p : players) {
            table.addPlayer(p);
        }
    }

    public int startRound() throws ExecutionException, InterruptedException {
        table.setDeck(new Deck());
        table.setCommonCards(new ArrayList<>());
        for(Player p : table.getPlayersInRound().keySet()){
            p.clearHole();
        }
        table.setPotSize(0);
        table.setWinner(null);
        table.setRound(Table.ROUND.PREFLOP);
        System.out.println("Start round");
        for (Map.Entry<Player, Boolean> entry : table.getPlayersInRound().entrySet()) {
            entry.setValue(true);
        }
        table.setDealerIndex(table.getDealerIndex() + 1);
        table.setCurrentPlayerIndex(table.getDealerIndex()+1);
        Player[] players = table.getPlayerBets().keySet().toArray(new Player[0]);
        //Tell player left of dealer he may fold or post the small blind
        Turn lastTurn = new Turn(null, null, 0);

        while (players.length > 0) {
            players[(table.getDealerIndex() + 1) % players.length].setRequiredBet(table.getSmallBlind());
            lastTurn = players[(table.getDealerIndex() + 1) % players.length].playTurn(
                    new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, CALL),
                            0, table.getSmallBlind(), players[(table.getDealerIndex() + 1) % players.length]), this);
            if (lastTurn.getAction() == Turn.PlayerAction.FOLD) {
                table.getPlayersInRound().put(lastTurn.getPlayer(), false);
                List<Player> nonFoldedPlayers = new ArrayList<>();
                for (Player p : table.getPlayersInRound().keySet()) {
                    if (table.getPlayersInRound().get(p)) {
                        nonFoldedPlayers.add(p);
                    }
                }
                players = nonFoldedPlayers.toArray(new Player[0]);
                table.setCurrentPlayerIndex(table.getCurrentPlayerIndex() + 1);
            } else {
                break;
            }
        }
        if (players.length == 0) {
            return 0;
        }
        receivePlayerTurn(lastTurn);
        players[(table.getDealerIndex() + 2) % players.length].setRequiredBet(table.getBigBlind());
        lastTurn = players[(table.getDealerIndex() + 2) % players.length].playTurn(
                new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, CALL),
                        0, table.getBigBlind(), players[(table.getDealerIndex() + 2) % players.length]), this);

        for (Player p : players) {
            p.drawHole(table.getDeck());
            System.out.println(p.getName() + ", your hand is: " + p.getHoleAsString() + ". You have " + p.getChips() + " chips.");
        }

        receivePlayerTurn(lastTurn);

        System.out.println("POT AMOUNT: " + table.getPotSize());

        table.setCurrentPlayerIndex(table.getDealerIndex() + 3);
        lastTurn = players[table.getCurrentPlayerIndex() % players.length].playTurn(
                new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, CALL, Turn.PlayerAction.RAISE),
                        table.getPlayerBets().get(players[(table.getCurrentPlayerIndex() - 1) % players.length]), 0, players[table.getCurrentPlayerIndex() % players.length]), this);
        receivePlayerTurn(lastTurn);

        winnerInfo = "Winner: " + table.getWinner().getName() + " with a " + handTypes[table.getWinner().bestHand(table.getCommonCards().toArray(new Card[0])) - 1];

        System.out.println("Winner: " + table.getWinner().getName() + " with a " + handTypes[table.getWinner().bestHand(table.getCommonCards().toArray(new Card[0])) - 1]);
        table.getWinner().receiveWinnings(table.getPotSize());

        for (Player p : players) {
            System.out.println(p.getName() + ", your hand was: " + p.getHoleAsString() + ". You now have " + p.getChips() + " chips.");
        }
        Thread.sleep(10000);
        return 1;
    }

    @Async
    private void receivePlayerTurn(Turn t) throws ExecutionException, InterruptedException {
        //note: fold needs to remove you from lists and set you to not playing
        //need to check if all players but one have folded
        //if so, end the game
        System.out.println("ROUND: " + table.getRound());
        table.setPotSize(table.getPotSize() + t.getBetAmount());
        if (t.getBetAmount() > 0) {
            table.getPlayerBets().put(t.getPlayer(), t.getBetAmount());
        }
        if (t.getAction() == Turn.PlayerAction.FOLD) {
            table.getPlayerBets().remove(t.getPlayer());
            table.getPlayersInRound().put(t.getPlayer(), false);
        }
        System.out.println("TABLE RECEIVED ACTION OF\n\t " + t.toString());
        List<Player> nonFoldedPlayers = new ArrayList<>();
        for (Player p : table.getPlayersInRound().keySet()) {
            if (table.getPlayersInRound().get(p)) {
                nonFoldedPlayers.add(p);
            }
        }
        Player[] players = nonFoldedPlayers.toArray(new Player[0]);
        //Player[] players = playersInRound.keySet().toArray(new Player[0]);
        table.setCurrentPlayerIndex(table.getCurrentPlayerIndex() + 1);

        //go to next round once the dealer plays his turn
        if (table.getCurrentPlayerIndex() == table.getDealerIndex() + players.length + 1) {
            switch (table.getRound()) {
                case PREFLOP:
                    table.setRound(Table.ROUND.FLOP);
                    for (int i = 0; i < 3; i++) {
                        table.getCommonCards().add(table.getDeck().next());
                    }
                    System.out.println("COMMON CARDS: " + table.getCommonCards().toString());
                    break;
                case FLOP:
                    table.setRound(Table.ROUND.TURN);
                    table.getCommonCards().add(table.getDeck().next());
                    System.out.println("COMMON CARDS: " + table.getCommonCards().toString());
                    break;
                case TURN:
                    table.setRound(Table.ROUND.RIVER);
                    table.getCommonCards().add(table.getDeck().next());
                    System.out.println("COMMON CARDS: " + table.getCommonCards().toString());
                    break;
                //showdown time -- implement end of game logic
                case RIVER:
                    table.setRound(Table.ROUND.INTERIM);
                    int bestHand = 10;
                    for (int i = 0; i < players.length; i++) {
                        if (players[i].bestHand(table.getCommonCards().toArray(new Card[0])) < bestHand) {
                            bestHand = players[i].bestHand(table.getCommonCards().toArray(new Card[0]));
                            table.setWinner(players[i]);
                        }
                    }
                    break;
            }
            if (!(table.getRound() == Table.ROUND.INTERIM)) {
                table.getPlayerBets().clear();
                table.setDealerIndex(table.getDealerIndex() + 1);
                table.setCurrentPlayerIndex(table.getDealerIndex() + 1);
                receivePlayerTurn(players[table.getCurrentPlayerIndex() % players.length].playTurn(
                        new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CHECK, Turn.PlayerAction.RAISE),
                                0, 0, players[table.getCurrentPlayerIndex() % players.length]), this));
            }
        } else {

            //In the preflop, if we have passed the blinds and players have received their cards
            //and we are still in the same stage (haven't passed dealer), start passing turns around until we get back to the dealer
            if (table.getRound() == Table.ROUND.PREFLOP && players[table.getCurrentPlayerIndex() % players.length].getHole()[0] != null && table.getCurrentPlayerIndex() > table.getDealerIndex() + 3 && table.getCurrentPlayerIndex() <= table.getDealerIndex() + players.length) {
                receivePlayerTurn(players[table.getCurrentPlayerIndex() % players.length].playTurn(
                        new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, CALL, Turn.PlayerAction.RAISE),
                                table.getPlayerBets().get(players[(table.getCurrentPlayerIndex() - 1) % players.length]), 0, players[table.getCurrentPlayerIndex() % players.length]), this));

            }
            if (!(table.getRound() == Table.ROUND.PREFLOP || table.getRound() == Table.ROUND.INTERIM)) {
                if (!table.getPlayerBets().containsKey(players[(table.getCurrentPlayerIndex() - 1) % players.length]) || table.getPlayerBets().get(players[(table.getCurrentPlayerIndex() - 1) % players.length]) == 0) {
                    receivePlayerTurn(players[table.getCurrentPlayerIndex() % players.length].playTurn(
                            new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, Turn.PlayerAction.CHECK, Turn.PlayerAction.RAISE),
                                    0, 0, players[table.getCurrentPlayerIndex() % players.length]), this));
                } else {
                    receivePlayerTurn(players[table.getCurrentPlayerIndex() % players.length].playTurn(
                            new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, CALL, Turn.PlayerAction.RAISE),
                                    table.getPlayerBets().get(players[(table.getCurrentPlayerIndex() - 1) % players.length]), 0, players[table.getCurrentPlayerIndex() % players.length]), this));
                }
            }
        }
    }

    @Async
    public Future<Turn> handleTurnNotification(TurnNotification t) {
        while(true){
            if (currentTurn != null) {
                Turn temp = currentTurn;
                currentTurn = null;
                return new AsyncResult<>(temp);
            } else {
                currentTurnNotification = t;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Turn getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(Turn currentTurn) {
        this.currentTurn = currentTurn;
    }

    public TurnNotification getCurrentTurnNotification() {
        return currentTurnNotification;
    }

    public void setCurrentTurnNotification(TurnNotification currentTurnNotification) {
        this.currentTurnNotification = currentTurnNotification;
    }
}
