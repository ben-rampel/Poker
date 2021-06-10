package webapp;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import poker.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static poker.Turn.PlayerAction.*;

@Service
public class TableController {
    private Table table;
    private Turn turn;
    private Turn newTurn;
    protected TurnNotification turnNotification;
    private int dealerSeed = 0;

    public TableController(Table table) {
        this.table = table;
        this.newTurn = null;
    }

    public synchronized void unblock(){
        this.notifyAll();
    }

    @Async
    public synchronized Future<Map<Player, Integer>> startRound() throws ExecutionException, InterruptedException {
        table = new TableImpl(this.table.getPlayers(), dealerSeed);
        dealerSeed++;
        turn = null;
        newTurn = null;
        turnNotification = null;
        System.out.println("-------Started Game-------");
        for (Player p : getTable().getPlayers()) {
            if (p.getChips() < 2) {
                p.setInRound(false);
            }
        }
        while (!table.hasTwoNext()) {
            for (Player p : getTable().getPlayers()) {
                if (p.getChips() < 2) {
                    p.setInRound(false);
                }
            }
            this.wait();
        }

        //Post Blinds
        table.drawHoleCards();
        table.next().setDealer(true);
        Player small_blind = getBlind(new SmallBlindNotification(null));
        getBlind(new BigBlindNotification(null));


        while (table.getRound().getRoundNum() < TableImpl.ROUND.INTERIM.getRoundNum()) {
            Player roundWinner = mainTurnLoop(table.getRound() == TableImpl.ROUND.BLINDS ? small_blind : null);
            if (roundWinner != null) {
                for (Player player : table.getPlayers()) {
                    if (player.isInRound()) table.setWinnerInfo(player.getName() + " mucks");
                }
                Map<Player, Integer> result = new HashMap<>();
                result.put(roundWinner, table.getPotSize());
                return new AsyncResult<>(result);
            }
            table.nextRound();
            if (table.getRound() == TableImpl.ROUND.INTERIM) {
                return new AsyncResult<>(table.getShowdownWinners());
            }
        }

        throw new IllegalStateException("round ended with no winner");
    }

    private synchronized Player mainTurnLoop(Player initial_player) throws InterruptedException {
        int turnsPlayed = 0;
        Player initialPlayer = initial_player;
        if (getTable().getRound() == TableImpl.ROUND.BLINDS){
            turnsPlayed = 2;
        }
        while (table.hasNext()) {
            Player next = table.next();
            if (turnsPlayed == 0) {
                initialPlayer = next;
            }
            if (table.activePlayers().size() < 2) {
                return table.next();
            }
            if (table.getCurrentBet() > 0 && next.getBet() == table.getCurrentBet()) {
                turn = new Turn(next, CHECK, 0);
                turnsPlayed++;
            } else {
                if (table.getCurrentBet() > 0) {
                    if (next.getBet() > 0) {
                        sendTurnNotification(new StakedTurnNotification(next, table.getCurrentBet() - next.getBet()));
                    } else {
                        sendTurnNotification(new StakedTurnNotification(next, table.getCurrentBet()));
                    }
                } else {
                    sendTurnNotification(new OpenTurnNotification(next));
                }
                while (newTurn == null) {
                    this.wait();
                    if (table.activePlayers().size() < 2) {
                        return table.next();
                    }
                }
                turnNotification = null;
                turn = newTurn;
                newTurn = null;
                turnsPlayed++;
                if (table.getCurrentBet() > 0) {
                    if (turn.getAction() == Turn.PlayerAction.CALL || turn.getAction() == Turn.PlayerAction.RAISE) {
                        turn.getPlayer().bet(turn.getBetAmount());
                        table.addToPot(turn.getBetAmount(), turn.getPlayer());
                        table.setCurrentBet(turn.getBetAmount());
                    } else if (turn.getAction() == ALLIN) {
                        int s = turn.getPlayer().getChips();
                        turn.getPlayer().bet(s);
                        Pot sidePot = table.getPots().get(table.getPots().size() - 1).split(turn.getPlayer(), s);
                        table.addPot(sidePot);

                    } else {
                        turn.getPlayer().setInRound(false);
                    }
                } else {
                    if (turn.getAction() == Turn.PlayerAction.BET) {
                        turn.getPlayer().bet(turn.getBetAmount());
                        table.addToPot(turn.getBetAmount(), turn.getPlayer());
                        table.setCurrentBet(turn.getBetAmount());
                    } else if (turn.getAction() == FOLD) {
                        turn.getPlayer().setInRound(false);
                        if (table.activePlayers().size() < 2) {
                            return table.next();
                        }
                    }
                }
            }
            if ((next == initialPlayer && turnsPlayed > 1) || turnsPlayed >= table.activePlayers().size()) {
                boolean allInPlayersMatchedBet = true;
                for (Player p : table.activePlayers()) {
                    if (p.getBet() < table.getCurrentBet() && p.getChips() != 0) allInPlayersMatchedBet = false;
                }
                if (allInPlayersMatchedBet) return null;
            }
        }
        throw new IllegalStateException("Turn loop ended without getting back to dealer or finding a winner");
    }

    private Player getBlind(TurnNotification blind) {
        Player currentPlayer = table.next();
        turn = new Turn(currentPlayer, BET, blind.getRequiredBet());
        currentPlayer.bet(blind.getRequiredBet());
        table.addToPot(blind.getRequiredBet(), turn.getPlayer());
        return currentPlayer;
    }

    public void sendTurnNotification(TurnNotification turnNotification) {
        if (turnNotification.getPlayer().getChips() < turnNotification.getMinimumBet() ||
                turnNotification.getPlayer().getChips() < turnNotification.getRequiredBet()) {
            turnNotification = new AllInTurnNotification(turnNotification.getPlayer());
        }
        this.turnNotification = turnNotification;
    }


    public Table getTable() {
        return table;
    }

    public synchronized void receiveTurn(Turn turn) {
        newTurn = turn;
        this.notifyAll();
    }

    TurnNotification getTurnNotification() {
        return turnNotification;
    }

    public void removePlayer(Player player) {
        if (turnNotification.getPlayer() == player) {
            receiveTurn(new Turn(player, FOLD, 0));
        }
        table.removePlayer(player);

    }
}
