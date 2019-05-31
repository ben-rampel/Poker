package webapp;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import poker.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static poker.Turn.PlayerAction.*;

public class PokerController {
    public String winnerInfo;
    private Table table;
    private Turn currentTurn;
    private TurnNotification currentTurnNotification;

    @Async
    public void startGame() {
        System.out.println("startgame");
        table = new Table();
        table.setRound(Table.ROUND.INTERIM);

        while (true) {
            if (table.getRound() == Table.ROUND.INTERIM) {
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

    public void addPlayer(Player p) {
        table.addPlayer(p);
    }

    public void removePlayer(Player p) {
        if (currentTurnNotification.getPlayer() == p) {
            currentTurn = new Turn(p, FOLD, 0);
        }
        table.removePlayer(p);
    }

    private void startRound() throws ExecutionException, InterruptedException {
        //Wait for 2 players to join the game
        System.out.println("Waiting for players");
        while (table.getPlayersInRound().size() < 2) {
            Thread.sleep(1000);
        }
        System.out.println("Starting");

        //Reset table properties and player hands, increment dealer index
        table = table.resetTable();
        for (Player p : table.getPlayersInRound().keySet()) {
            p.clearHole();
        }
        table.setDealerIndex((table.getDealerIndex() + 1) % nonFoldedPlayers().size());
        table.setCurrentPlayerIndex(table.getDealerIndex() + 1);

        Player[] players = nonFoldedPlayers().toArray(new Player[0]);
        Turn lastTurn = new Turn(null, null, 0);

        //Tell player left of dealer he may fold or post the small blind
        //If he folds, set next player in line as small blind and repeat
        while (players.length > 0) {
            lastTurn = players[(table.getDealerIndex() + 1) % players.length].playTurn(
                    new TurnNotification(
                            Arrays.asList(FOLD, CALL),
                            0,
                            table.getSmallBlind(),
                            players[(table.getDealerIndex() + 1) % players.length]),
                    this);

            //Remove player if he folds
            if (lastTurn.getAction() == FOLD) {
                table.getPlayersInRound().put(lastTurn.getPlayer(), false);
                table.getPlayerBets().remove(lastTurn.getPlayer());
                //End game if second to last player folds
                if (table.getPlayerBets().size() < 2) {
                    Player player = table.getPlayerBets().keySet().toArray(new Player[0])[0];
                    winnerInfo = player.getName() + " mucks";
                    table.setRound(Table.ROUND.INTERIM);
                    table.setWinner(player);
                    return;
                }
                //List<Player> nonFoldedPlayers = nonFoldedPlayers();
                //players = nonFoldedPlayers.toArray(new Player[0]);
                table.setCurrentPlayerIndex(table.getCurrentPlayerIndex() + 1);
            } else {
                break;
            }
            players = nonFoldedPlayers().toArray(new Player[0]);
        }
        if (players.length == 0) {
            return;
        }
        //Big Blind logic
        //TODO make it go to the next person if the big blind folds like how the small blind currently works
        receivePlayerTurn(lastTurn);
        lastTurn = players[(table.getDealerIndex() + 2) % players.length].playTurn(
                new TurnNotification(Arrays.asList(FOLD, CALL),
                        0, table.getBigBlind(), players[(table.getDealerIndex() + 2) % players.length]), this);

        for (Player p : players) {
            p.drawHole(table.getDeck());
        }

        receivePlayerTurn(lastTurn);


        table.setCurrentPlayerIndex(table.getDealerIndex() + 3);
        if (players.length > 2) {
            lastTurn = players[table.getCurrentPlayerIndex() % players.length].playTurn(
                    new TurnNotification(Arrays.asList(FOLD, CALL, Turn.PlayerAction.RAISE),
                            table.getPlayerBets().get(players[(table.getCurrentPlayerIndex() - 1) % players.length]), 0, players[table.getCurrentPlayerIndex() % players.length]), this);
            receivePlayerTurn(lastTurn);
        }

        System.out.println("yeet");


        if (nonFoldedPlayers().size() > 1) {
            winnerInfo = "Winner: " + table.getWinner().getName() + " with a " + table.getWinner().bestHand(table.getCommonCards().toArray(new Card[0]));
        }
        table.getWinner().receiveWinnings(table.getPotSize());


        Thread.sleep(10000);
    }

    private List<Player> nonFoldedPlayers() {
        List<Player> nonFoldedPlayers = new ArrayList<>();
        for (Player p : table.getPlayersInRound().keySet()) {
            if (table.getPlayersInRound().get(p)) {
                nonFoldedPlayers.add(p);
            }
        }
        return nonFoldedPlayers;
    }

    @Async
    public void receivePlayerTurn(Turn t) throws ExecutionException, InterruptedException {
        List<Player> nonFoldedPlayers = nonFoldedPlayers();

        //TODO: end game if all players but one fold

        System.out.println("ROUND: " + table.getRound());
        table.setPotSize(table.getPotSize() + t.getBetAmount());
        if (t.getBetAmount() > 0) {
            table.getPlayerBets().put(t.getPlayer(), t.getBetAmount());
        }
        if (t.getAction() == FOLD) {
            table.getPlayerBets().remove(t.getPlayer());
            table.getPlayersInRound().put(t.getPlayer(), false);
            nonFoldedPlayers.remove(t.getPlayer());
            System.out.println(nonFoldedPlayers);
            if (nonFoldedPlayers.size() < 2) {
                System.out.println("h");
                Player player = nonFoldedPlayers.get(0);
                winnerInfo = player.getName() + " mucks";
                table.setRound(Table.ROUND.INTERIM);
                table.setWinner(player);
                return;
            }
        }
        System.out.println("TABLE RECEIVED ACTION OF\n\t " + t.toString());

        Player[] players = nonFoldedPlayers().toArray(new Player[0]);
        //if(t.getAction() != FOLD)
        table.setCurrentPlayerIndex(table.getCurrentPlayerIndex() + 1);

        //go to next round once the dealer plays his turn
        if (table.getCurrentPlayerIndex() == table.getDealerIndex() + players.length + 1) {
            System.out.println("p");
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
                    TreeMap<Hand, Player> bestHandMap = new TreeMap<>();
                    for(Player p : nonFoldedPlayers()){
                        bestHandMap.put(p.bestHand(table.getCommonCards().toArray(new Card[0])),p);
                    }
                    table.setWinner(bestHandMap.lastEntry().getValue());
                    break;
            }
            if (!(table.getRound() == Table.ROUND.INTERIM)) {
                table.getPlayerBets().clear();
                table.setDealerIndex(table.getDealerIndex() + 1);
                table.setCurrentPlayerIndex(table.getDealerIndex() + 1);
                receivePlayerTurn(players[table.getCurrentPlayerIndex() % players.length].playTurn(
                        new TurnNotification(Arrays.asList(FOLD, Turn.PlayerAction.CHECK, Turn.PlayerAction.RAISE),
                                0, 0, players[table.getCurrentPlayerIndex() % players.length]), this));

            }
        } else {

            //In the preflop, if we have passed the blinds and players have received their cards
            //and we are still in the same stage (haven't passed dealer), start passing turns around until we get back to the dealer
            if (table.getRound() == Table.ROUND.PREFLOP && players[table.getCurrentPlayerIndex() % players.length].getHole()[0] != null && table.getCurrentPlayerIndex() > table.getDealerIndex() + 3 && table.getCurrentPlayerIndex() <= table.getDealerIndex() + players.length) {
                receivePlayerTurn(players[table.getCurrentPlayerIndex() % players.length].playTurn(
                        new TurnNotification(Arrays.asList(FOLD, CALL, Turn.PlayerAction.RAISE),
                                table.getPlayerBets().get(players[(table.getCurrentPlayerIndex() - 1) % players.length]), 0, players[table.getCurrentPlayerIndex() % players.length]), this));

            }
            if (!(table.getRound() == Table.ROUND.PREFLOP || table.getRound() == Table.ROUND.INTERIM)) {
                if (!table.getPlayerBets().containsKey(players[(table.getCurrentPlayerIndex() - 1) % players.length]) || table.getPlayerBets().get(players[(table.getCurrentPlayerIndex() - 1) % players.length]) == 0) {
                    receivePlayerTurn(players[table.getCurrentPlayerIndex() % players.length].playTurn(
                            new TurnNotification(Arrays.asList(FOLD, Turn.PlayerAction.CHECK, Turn.PlayerAction.RAISE),
                                    0, 0, players[table.getCurrentPlayerIndex() % players.length]), this));
                } else {
                    receivePlayerTurn(players[table.getCurrentPlayerIndex() % players.length].playTurn(
                            new TurnNotification(Arrays.asList(FOLD, CALL, Turn.PlayerAction.RAISE),
                                    table.getPlayerBets().get(players[(table.getCurrentPlayerIndex() - 1) % players.length]), 0, players[table.getCurrentPlayerIndex() % players.length]), this));
                }
            }
        }
    }

    @Async
    public Future<Turn> handleTurnNotification(TurnNotification t) {
        while (true) {
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
