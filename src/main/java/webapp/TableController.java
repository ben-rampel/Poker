package webapp;

import poker.*;

import java.util.*;

import static poker.Turn.PlayerAction.*;

public class TableController extends Observable {
    private Table table = new TableImpl();
    private final Queue<Turn> turns = new LinkedList<>();
    private Player initialPlayer = null;
    private int turnsPlayed = 0;
    protected TurnNotification turnNotification;
    private int dealerSeed = 0;
    public enum State {STARTING, READY, PROCESSING, DONE}
    private State state = State.STARTING;

    public synchronized Map<Player, Integer> startRound() {
        initialize();
        while (table.getRound().compareTo(TableImpl.ROUND.INTERIM) < 0 && table.activePlayers().size() >= 2) {
            ready();
        }
        setState(State.DONE);
        if (table.activePlayers().size() == 1) {
            return muckResults();
        } else {
            return table.getShowdownWinners();
        }
    }

    public synchronized void initialize() {
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
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }
        table.setRound(TableImpl.ROUND.BLINDS);
        //Post Blinds
        table.drawHoleCards();
        table.next().setDealer(true);
        initialPlayer = getBlind(new SmallBlindNotification(null));
        getBlind(new BigBlindNotification(null));
        if (table.activePlayers().size() == 2) {
            Player n;
            turnNotification = new LockedTurnNotification(n = table.next(), table.getCurrentBet() - n.getBet());
        } else {
            turnNotification = new BigBlindNotification(table.next());
        }
    }

    private synchronized void ready() {
        setState(State.READY);
        while (turns.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }
        TurnNotification result = processTurn(turns.poll());
        setState(State.READY);
        setTurnNotification(result);
    }

    private synchronized TurnNotification processTurn(Turn turn) {
        setState(State.PROCESSING);
        boolean wasTurnPlayed = true;
        Player player = turn.getPlayer();
        int betAmount = turn.getBetAmount();
        int playerChips = player.getChips();

        //Process received turn
        if (turn.isFold()) {
            wasTurnPlayed = false;
            player.setInRound(false);
        } else if (player.isAllIn()) {
            Pot lastPot = table.getLastPot();
            if (lastPot.hasPlayer(player)) {
                table.addPot(lastPot.split(player, 0));
            }
        } else if (betAmount >= playerChips || turn.isAllIn()) {
            player.setAllIn(true);
            table.handleBet(player,playerChips);
            table.splitLastPot(player,playerChips);
        } else if (!turn.isCheck()){
            table.handleBet(player,betAmount);
        }

        // Figure out next turn
        if(wasTurnPlayed) turnsPlayed++;
        if (table.activePlayers().size() < 2) {
            return null;
        }
        Player next = table.next();
        if (((next == initialPlayer && turnsPlayed > 1)
                || turnsPlayed >= table.activePlayers().size())) {
            // Can we go to the next round?
            boolean allInPlayersMatchedBet = true;
            for (Player p : table.activePlayers()) {
                if (p.getBet() < table.getCurrentBet() && !p.isAllIn()) allInPlayersMatchedBet = false;
            }
            if (allInPlayersMatchedBet) {
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
        table.handleBet(currentPlayer,blind.getRequiredBet());
        return currentPlayer;
    }

    public synchronized void setTurnNotification(TurnNotification turnNotification) {
        waitForReady();
        if (turnNotification == null) {
            this.turnNotification = null;
            return;
        }
        Player p = turnNotification.getPlayer();
        if (!p.isAllIn() && (p.getChips() < turnNotification.getMinimumBet() || p.getChips() < turnNotification.getRequiredBet())) {
            this.turnNotification = new AllInTurnNotification(p);
        } else if (p.isAllIn()) {
            this.turnNotification = new OpenTurnNotification(p);
            receiveTurn(new Turn(p, CHECK, 0));
        } else {
            this.turnNotification = turnNotification;
        }
    }

    public synchronized Table getTable() {
        while (state == State.PROCESSING) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
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
        if (t.getPlayer() != turn.getPlayer()) {
            setState(State.READY);
            throw new RuntimeException("Invalid turn");
        }
        turns.add(turn);
        notifyAll();
    }

    public synchronized void addPlayer(Player player) {
        this.table.addPlayer(player);
        notifyAll();
    }

    public synchronized void removePlayer(Player player) {
        if (turnNotification != null && turnNotification.getPlayer() == player) {
            receiveTurn(new Turn(player, FOLD, 0));
        }
        table.removePlayer(player);
    }

    private void setState(State state) {
        this.state = state;
        //System.out.println(state);
        setChanged();
        notifyAll();
        notifyObservers(state);
    }

    public synchronized State getState() {
        waitForReady();
        return state;
    }

    public Map<Player, Integer> muckResults() {
        if (table.activePlayers().size() != 1) {
            throw new IllegalStateException();
        }
        Player winner = table.activePlayers().toArray(new Player[0])[0];
        table.setWinnerInfo(winner.getName() + " mucks");
        Map<Player, Integer> result = new HashMap<>();
        result.put(winner, table.getTotalPotAmount());
        return result;
    }

    public synchronized void waitForReady() {
        while (state == State.STARTING || state == State.PROCESSING) {
            try {
                this.wait();
            } catch (InterruptedException ignored) {}
        }
    }
}
