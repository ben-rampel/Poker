package webapp;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import poker.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static poker.Turn.PlayerAction.BET;
import static poker.Turn.PlayerAction.FOLD;

public class TableController {
    private Table table;
    private Turn turn;
    private Turn newTurn;
    private TurnNotification turnNotification;
    private int dealerSeed = 0;

    public TableController(Table table) {
        this.table = table;
        this.newTurn = null;
    }

    @Async
    public Future<Player> startRound() throws ExecutionException, InterruptedException {
        table = new TableImpl(this.table.getPlayers(),dealerSeed);
        dealerSeed++;
        turn = null;
        newTurn = null;
        turnNotification = null;
        System.out.println("started");
        while (!table.hasTwoNext()) {
            Thread.sleep(10000);
            System.out.println("waiting for players...");
        }
        //Post Blinds

        table.drawHoleCards();
        table.next().setDealer(true);
        getBlind(new SmallBlindNotification(null));
        getBlind(new BigBlindNotification(null));

        if (turn.getBetAmount() != utils.bigBlind)
            throw new AssertionError("last bet amount not equal to big blind amount after big blind");

        table.setCurrentBet((table.activePlayers().size() > 2) ? utils.bigBlind : 0);

        table.nextRound();
        //if (table.activePlayers().size() < 3) table.nextRound();

        while (table.getRound().getRoundNum() < TableImpl.ROUND.INTERIM.getRoundNum()) {
            Player roundWinner = mainTurnLoop();
            if (roundWinner != null) {
                return new AsyncResult<>(roundWinner);
            }
            table.nextRound();
            if (table.getRound() == TableImpl.ROUND.INTERIM) {
                table.getShowdownWinner();
                return new AsyncResult<>(table.getShowdownWinner());
            }
        }

        throw new IllegalStateException("round ended with no winner");
    }

    private Player mainTurnLoop() throws ExecutionException, InterruptedException {
        int turnsPlayed = 0;
        while (table.hasNext()) {
            Player next = table.next();
            Future<Turn> t;
            if (table.getCurrentBet() > 0) {
                if(next.getBet() > 0) {
                    t = sendTurnNotification(new LockedTurnNotification(next, table.getCurrentBet() - next.getBet()));
                } else {
                    t = sendTurnNotification(new StakedTurnNotification(next, table.getCurrentBet()));
                }
            } else {
                t = sendTurnNotification(new OpenTurnNotification(next));
            }
            for (int i = 0; i < 1000; ) {
                if (t.isDone()) {
                    turn = t.get();
                    turnsPlayed++;
                    break;
                } else {
                    Thread.sleep(100);
                    i++;
                }
            }
            if (table.getCurrentBet() > 0) {
                if (turn.getAction() == Turn.PlayerAction.CALL || turn.getAction() == Turn.PlayerAction.RAISE) {
                    turn.getPlayer().bet(turn.getBetAmount());
                    table.addToPot(turn.getBetAmount());
                    table.setCurrentBet(turn.getBetAmount());
                } else {
                    turn.getPlayer().setInRound(false);
                    if (table.activePlayers().size() < 2) {
                        return table.next();
                    }
                }
            } else {
                if (turn.getAction() == Turn.PlayerAction.BET) {
                    turn.getPlayer().bet(turn.getBetAmount());
                    table.addToPot(turn.getBetAmount());
                    table.setCurrentBet(turn.getBetAmount());
                } else if (turn.getAction() == FOLD) {
                    turn.getPlayer().setInRound(false);
                    if (table.activePlayers().size() < 2) {
                        return table.next();
                    }
                }
            }
            if (next.isDealer() || turnsPlayed >= table.activePlayers().size()) {
                boolean allInPlayersMatchedBet = true;
                for(Player p : table.activePlayers()){
                    if(p.getBet() != table.getCurrentBet()) allInPlayersMatchedBet = false;
                }
                if(allInPlayersMatchedBet) return null;
            }
        }
        throw new IllegalStateException("Turn loop ended without getting back to dealer or finding a winner");
    }

    private void getBlind(TurnNotification blind) throws ExecutionException, InterruptedException {
        Player currentPlayer = table.next();
        turn = new Turn(currentPlayer, BET, blind.getRequiredBet());
        currentPlayer.bet(blind.getRequiredBet());
        table.addToPot(blind.getRequiredBet());
    }

    @Async
    public Future<Turn> sendTurnNotification(TurnNotification turnNotification) {
        this.turnNotification = turnNotification;
        while (newTurn == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Future<Turn> result = new AsyncResult<>(newTurn);
        newTurn = null;
        this.turnNotification = null;
        return result;
    }

    public Table getTable() {
        return table;
    }

    public void receiveTurn(Turn turn) {
        newTurn = turn;
    }

    TurnNotification getTurnNotification() {
        return turnNotification;
    }

    public void removePlayer(Player player){
        if(turnNotification.getPlayer() == player){
            receiveTurn(new Turn(player, FOLD,0));
        }
        table.removePlayer(player);

    }
}
