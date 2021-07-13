package webapp;

import poker.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static poker.Turn.PlayerAction.*;

public class TableController extends Observable {
    private Table table = new TableImpl();
    private Queue<Turn> turns = new LinkedList<>();
    private Player initialPlayer = null;
    private int turnsPlayed = 0;
    protected TurnNotification turnNotification;
    private int dealerSeed = 0;
    public enum State {STARTING, READY, PROCESSING, DONE}
    private State state = State.STARTING;

    public synchronized Map<Player, Integer> startRound() throws ExecutionException, InterruptedException{
        initialize();
        while(table.getRound().compareTo(TableImpl.ROUND.INTERIM) < 0 && table.activePlayers().size() >= 2){
            ready();
        }
        setState(State.DONE);
        if (table.activePlayers().size() == 1) {
            return muckResults();
        } else {
            return table.getShowdownWinners();
        }
    }

    public synchronized void initialize() throws InterruptedException {
        setState(State.STARTING);
        table = new TableImpl(this.table.getPlayers(), dealerSeed++);
        turns.clear();
        turnNotification = null;
        table.getPlayers().stream().filter(p -> p.getChips() < 2).forEach(p -> p.setInRound(false));
        table.getPlayers().forEach(p -> p.setAllIn(false));
        table.setRound(TableImpl.ROUND.INTERIM);
        //Wait for 2 players to join
        while (!table.hasTwoNext()) {
            for (Player p : table.getPlayers()) {
                if (p.getChips() < 2) {
                    p.setInRound(false);
                }
            }
            this.wait();
        }
        table.setRound(TableImpl.ROUND.BLINDS);
        //Post Blinds
        table.drawHoleCards();
        table.next().setDealer(true);
        initialPlayer = getBlind(new SmallBlindNotification(null));
        getBlind(new BigBlindNotification(null));
        if(table.activePlayers().size() == 2){
            Player n;
            turnNotification = new LockedTurnNotification(n = table.next(), table.getCurrentBet() - n.getBet());
        } else {
            turnNotification = new BigBlindNotification(table.next());
        }
    }

    private synchronized void ready() throws InterruptedException {
        setState(State.READY);
        while(turns.isEmpty()){
            wait();
        }
        TurnNotification result = processTurn(turns.poll());
        setState(State.READY);
        setTurnNotification(result);
    }

    private synchronized TurnNotification processTurn(Turn turn){
        if(turn.getBetAmount() == turn.getPlayer().getChips() && turn.getBetAmount() > 0) {
            turn.getPlayer().setAllIn(true);
            turn = new Turn(turn.getPlayer(), ALLIN, turn.getBetAmount());
        }
        System.out.println(turn);
        setState(State.PROCESSING);
        if (table.getCurrentBet() > 0) {
            if (turn.getAction() == Turn.PlayerAction.CALL || turn.getAction() == Turn.PlayerAction.RAISE) {
                turn.getPlayer().bet(turn.getBetAmount());
                table.addToPot(turn.getBetAmount(), turn.getPlayer());
                table.setCurrentBet(Math.max(turn.getBetAmount(), table.getCurrentBet()));
            } else if (turn.getAction() == ALLIN) {
                int s = turn.getPlayer().getChips();
                turn.getPlayer().bet(s);
                table.addToPot(s, turn.getPlayer());
                Pot sidePot = table.getPots().get(table.getPots().size()-1).split(turn.getPlayer(), s);
                table.addPot(sidePot);
            } else if (turn.getPlayer().isAllIn()) {
                Pot p = table.getPots().get(table.getPots().size()-1);
                if (p.getBets().containsKey(turn.getPlayer())){
                    Pot sidePot = p.split(turn.getPlayer(), 0);
                    table.addPot(sidePot);
                }
            } else {
                turnsPlayed--;
                turn.getPlayer().setInRound(false);
            }
        } else {
            if (turn.getAction() == Turn.PlayerAction.BET) {
                turn.getPlayer().bet(turn.getBetAmount());
                table.addToPot(turn.getBetAmount(), turn.getPlayer());
                table.setCurrentBet(turn.getBetAmount());
            } else if (turn.getAction() == FOLD) {
                turnsPlayed--;
                turn.getPlayer().setInRound(false);
            }
        }
        turnsPlayed++;
        if(table.activePlayers().size() < 2){
            return null;
        }
        Player next = table.next();
        if (turn.getAction() != RAISE && turn.getAction() != BET && ((next == initialPlayer && turnsPlayed > 1) || turnsPlayed >= table.activePlayers().size())) {
            boolean allInPlayersMatchedBet = true;
            for (Player p : table.activePlayers()) {
                if (p.getBet() < table.getCurrentBet() && !p.isAllIn()) allInPlayersMatchedBet = false;
            }
            if(allInPlayersMatchedBet){
                table.nextRound();
                turnsPlayed = 0;
            }
        }
        if (table.getCurrentBet() > 0) {
            if (next.getBet() > 0) {
                return new StakedTurnNotification(next, table.getCurrentBet() - next.getBet());
            } else {
                return new StakedTurnNotification(next, table.getCurrentBet());
            }
        } else {
            return new OpenTurnNotification(next);
        }
    }

    private synchronized Player getBlind(TurnNotification blind) {
        Player currentPlayer = table.next();
        turnsPlayed++;
        currentPlayer.bet(blind.getRequiredBet());
        table.addToPot(blind.getRequiredBet(), currentPlayer);
        return currentPlayer;
    }

    public synchronized void setTurnNotification(TurnNotification turnNotification) {
        waitForReady();
        if(turnNotification == null){
            this.turnNotification = null;
            return;
        }
        Player p = turnNotification.getPlayer();
        if (!p.isAllIn() && p.getChips() < turnNotification.getMinimumBet() || p.getChips() < turnNotification.getRequiredBet()) {
            this.turnNotification = new AllInTurnNotification(p);
        } else if (p.isAllIn()){
            this.turnNotification = new OpenTurnNotification(p);
            receiveTurn(new Turn(p, CHECK, 0));
        } else {
            this.turnNotification = turnNotification;
        }
    }

    public synchronized Table getTable() {
        while(state == State.PROCESSING) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return table;
    }

    public synchronized TurnNotification getTurnNotification() {
        waitForReady();
        return turnNotification;
    }

    public synchronized void receiveTurn(Turn turn) {
        if (state == State.DONE) {
            return;
        }
        TurnNotification t = getTurnNotification();
        setState(State.PROCESSING);
        System.out.println(turnNotification);
        if (turn.getBetAmount() > turn.getPlayer().getChips()) {
            setState(State.READY);
            throw new BetValueException(turn.getPlayer());
        }
        if(t.getPlayer() != turn.getPlayer()){
            setState(State.READY);
            throw new RuntimeException("Invalid turn");
        }
        turns.add(turn);
        notifyAll();
    }

    public synchronized void addPlayer(Player player){
        this.table.addPlayer(player);
        notifyAll();
    }

    public synchronized void removePlayer(Player player) {
        if (turnNotification != null && turnNotification.getPlayer() == player) {
            receiveTurn(new Turn(player, FOLD, 0));
        }
        table.removePlayer(player);
    }

    private void setState(State state){
        this.state = state;
        //System.out.println(state);
        setChanged();
        notifyAll();
        notifyObservers(state);
    }

    public synchronized State getState(){
        waitForReady();
        return state;
    }

    public Map<Player, Integer> muckResults(){
        if (table.activePlayers().size() != 1) {
            throw new IllegalStateException();
        }
        Player winner = table.activePlayers().toArray(new Player[0])[0];
        table.setWinnerInfo(winner.getName() + " mucks");
        Map<Player, Integer> result = new HashMap<>();
        result.put(winner, table.getTotalPotAmount());
        return result;
    }

    public synchronized void waitForReady(){
        while(state == State.STARTING || state == State.PROCESSING) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
