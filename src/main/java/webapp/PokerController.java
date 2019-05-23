package webapp;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import poker.*;

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
    public void startGame() {
        System.out.println("startgame");
        table = new Table();
        table.setRound(Table.ROUND.INTERIM);
/*        List<Player> playerList = new LinkedList<>();
        playerList.add(new Player("John", 250));
        playerList.add(new Player("Bob", 250));
        playerList.add(new Player("Carl", 250));
        playerList.add(new Player("Joe", 250));
        for (Player p : playerList) {
            table.addPlayer(p);
        }*/
        while(true){
            if(table.getRound() == Table.ROUND.INTERIM) {
                try {
                    startRound();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void addPlayer(Player p){
        table.addPlayer(p);
    }

    public void removePlayer(Player p){
        if(currentTurnNotification.getPlayer() == p){
            currentTurn = new Turn(p, Turn.PlayerAction.FOLD, 0);
        }
        table.removePlayer(p);
    }

    public void startRound() throws ExecutionException, InterruptedException {
        System.out.println("Waiting for players");
        while(table.getPlayersInRound().size() < 2){
            Thread.sleep(1000);
        }
        System.out.println("Starting");

        table = table.resetTable();
        for(Player p : table.getPlayersInRound().keySet()){
            p.clearHole();
        }
        table.setDealerIndex(table.getDealerIndex() + 1);
        table.setCurrentPlayerIndex(table.getDealerIndex()+1);
        Player[] players = table.getPlayerBets().keySet().toArray(new Player[0]);

        //Tell player left of dealer he may fold or post the small blind
        Turn lastTurn = new Turn(null, null, 0);

        while (players.length > 0) {
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
            return;
        }
        receivePlayerTurn(lastTurn);
        lastTurn = players[(table.getDealerIndex() + 2) % players.length].playTurn(
                new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, CALL),
                        0, table.getBigBlind(), players[(table.getDealerIndex() + 2) % players.length]), this);

        for (Player p : players) {
            p.drawHole(table.getDeck());
        }

        receivePlayerTurn(lastTurn);


        table.setCurrentPlayerIndex(table.getDealerIndex() + 3);
        if(players.length > 2) {
            lastTurn = players[table.getCurrentPlayerIndex() % players.length].playTurn(
                    new TurnNotification(Arrays.asList(Turn.PlayerAction.FOLD, CALL, Turn.PlayerAction.RAISE),
                            table.getPlayerBets().get(players[(table.getCurrentPlayerIndex() - 1) % players.length]), 0, players[table.getCurrentPlayerIndex() % players.length]), this);
            receivePlayerTurn(lastTurn);
        }
        System.out.println("yeet");
        winnerInfo = "Winner: " + table.getWinner().getName() + " with a " + handTypes[table.getWinner().bestHand(table.getCommonCards().toArray(new Card[0])) - 1];

        table.getWinner().receiveWinnings(table.getPotSize());


        Thread.sleep(10000);
    }

    @Async
    private void receivePlayerTurn(Turn t) throws ExecutionException, InterruptedException {
        if(t.getBetAmount() > t.getPlayer().getChips()){
            throw new IllegalArgumentException();
        }

        //TODO: end game if all players but one fold

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
