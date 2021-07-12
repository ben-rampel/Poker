package pokertest;

import org.junit.Test;
import poker.Player;
import poker.Pot;
import poker.TableImpl;
import poker.Turn;
import webapp.Lobby;
import webapp.LobbyImpl;
import webapp.TableController;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class lobbyTest {
    final List<Lobby> lobbies = new LinkedList<>();
    final Lobby test = new LobbyImpl();

    @Test
    public void test1() {
        String[] names = {"Ben", "Matt", "Joe", "Chuck", "Taro", "Natsuki"};
        for (int i = 0; i < 3; i++) {
            lobbies.add(new LobbyImpl());
            lobbies.get(i).start();
            lobbies.get(i).addPlayer(new Player(names[i * 2], 250));
            lobbies.get(i).addPlayer(new Player(names[i * 2 + 1], 250));
        }

        for (int i = 0; i < 3; i++) {
            Lobby lobby = lobbies.get(i);
            Player first = lobby.getPlayers().get(0);
            lobby.receiveTurn(new Turn(first, Turn.PlayerAction.CALL, 1));
        }

        for (int i = 2; i >= 0; i--) {
            Lobby lobby = lobbies.get(i);
            Player first = lobby.getPlayers().get(0);
            Player second = lobby.getPlayers().get(1);
            lobby.receiveTurn(new Turn(second, Turn.PlayerAction.BET, 20));
            lobby.receiveTurn(new Turn(first, Turn.PlayerAction.FOLD, 0));
        }
        for (int i = 2; i >= 0; i--) {
            Lobby lobby = lobbies.get(i);
            Player second = lobby.getPlayers().get(1);
            lobby.awaitWinner();
            assertEquals(lobby.getState().getWinner().getName(), second.getName());
        }
    }

    @Test
    public void test2() {
        TableController tableController = new TableController();
        tableController.getTable().addPlayer(new Player("Ben", 200));
        tableController.getTable().addPlayer(new Player("Brad", 200));
        Thread game = new Thread(() -> {
            try {
                tableController.startRound();
            } catch (Exception ignored) {
            }
        });
        game.start();
        tableController.receiveTurn(new Turn(tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CALL, 1));
        tableController.receiveTurn(new Turn(tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.BET, 20));
        tableController.receiveTurn(new Turn(tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.FOLD, 0));
        assertEquals(tableController.getState(), TableController.State.DONE);
        assertTrue(tableController.getTable().getWinnerInfo().contains("Brad"));
    }


    @Test
    public void test3() {
        TableController tableController = new TableController();
        Player ben, brad, bon, p;
        tableController.getTable().addPlayer(ben = new Player("Ben", 200));
        tableController.getTable().addPlayer(brad = new Player("Brad", 200));
        tableController.getTable().addPlayer(bon = new Player("Bon", 200));
        Player[] order = {ben, brad, bon};
        Thread game = new Thread(() -> {
            try {
                tableController.startRound();
            } catch (Exception ignored) {
            }
        });
        game.start();
        assertEquals(TableImpl.ROUND.BLINDS, tableController.getTable().getRound());
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CALL, 2));
        assertEquals(brad, p);
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CALL, 1));
        assertEquals(bon, p);
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.BET, 20));
        assertEquals(ben, p);
        assertEquals(TableImpl.ROUND.FLOP, tableController.getTable().getRound());
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.RAISE, 40));
        assertEquals(brad, p);
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.FOLD, 0));
        assertEquals(bon, p);
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CALL, 20));
        assertEquals(ben, p);
        assertEquals(TableImpl.ROUND.TURN, tableController.getTable().getRound());
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CHECK, 0));
        assertEquals(brad, p);
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.BET, 22));
        assertEquals(ben, p);
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CALL, 22));
        assertEquals(brad, p);
        assertEquals(TableImpl.ROUND.RIVER, tableController.getTable().getRound());
        assertEquals(TableController.State.READY, tableController.getState());
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CHECK, 0));
        assertEquals(ben, p);
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CHECK, 0));
        assertEquals(brad, p);
        assertEquals(TableImpl.ROUND.INTERIM, tableController.getTable().getRound());
        assertEquals(TableController.State.DONE, tableController.getState());
        System.out.println(tableController.getTable().getWinnerInfo() + " " + Arrays.toString(ben.getHole()) + " " + Arrays.toString(brad.getHole()) + " " + Arrays.toString(bon.getHole()));
    }

    @Test
    public void test4() {
        TableController tableController = new TableController();
        Player ben, brad, bon, p;
        tableController.getTable().addPlayer(ben = new Player("Ben", 200));
        tableController.getTable().addPlayer(brad = new Player("Brad", 200));
        tableController.getTable().addPlayer(bon = new Player("Bon", 200));
        Player[] order = {ben, brad, bon};
        Thread game = new Thread(() -> {
            try {
                tableController.startRound();
            } catch (Exception ignored) {
            }
        });
        game.start();
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.FOLD, 0));
        assertEquals(bon, tableController.getTurnNotification().getPlayer());
    }

    @Test
    public void test5() {
        Player ben, brad, bon, billy, bartholomew, p;
        TableController tableController = new TableController();
        tableController.addPlayer(ben = new Player("Ben", 200));
        tableController.addPlayer(brad = new Player("Brad", 200));
        tableController.addPlayer(bon = new Player("Bon", 200));
        tableController.addPlayer(billy = new Player("Billy", 200));
        tableController.addPlayer(bartholomew = new Player("Bartholomew", 200));
        Player[] order = {ben, brad, bon, billy, bartholomew};

        Thread game = new Thread(() -> {
            try {
                tableController.startRound();
            } catch (Exception ignored) {
            }
        });
        game.start();
        assertEquals(TableImpl.ROUND.BLINDS, tableController.getTable().getRound());
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CALL, 2));
        assertEquals(bartholomew, p);
        assertEquals(TableImpl.ROUND.BLINDS, tableController.getTable().getRound());
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CALL, 2));
        assertEquals(ben, p);
        assertEquals(TableImpl.ROUND.BLINDS, tableController.getTable().getRound());
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CALL, 2));
        assertEquals(brad, p);
        assertEquals(TableImpl.ROUND.BLINDS, tableController.getTable().getRound());
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CALL, 1));
        assertEquals(bon, p);
        assertEquals(TableImpl.ROUND.FLOP, tableController.getTable().getRound());
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.BET, 5));
        assertEquals(billy, p);
        assertEquals(TableImpl.ROUND.FLOP, tableController.getTable().getRound());
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.FOLD, 0));
        assertEquals(bartholomew, p);
        assertEquals(TableImpl.ROUND.FLOP, tableController.getTable().getRound());
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.FOLD, 0));
        assertEquals(ben, p);
        assertEquals(TableImpl.ROUND.FLOP, tableController.getTable().getRound());
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CALL, 5));
        assertEquals(brad, p);
        assertEquals(TableImpl.ROUND.FLOP, tableController.getTable().getRound());
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CALL, 5));
        assertEquals(bon, p);
        assertEquals(TableImpl.ROUND.TURN, tableController.getTable().getRound());
    }

    @Test
    public void allInTest(){
        Player ben, brad, p;
        TableController tableController = new TableController();
        tableController.addPlayer(ben = new Player("Ben", 1000));
        tableController.addPlayer(brad = new Player("Brad", 200));

        Thread game = new Thread(() -> {
            try {
                tableController.startRound();
            } catch (Exception ignored) {
            }
        });
        game.start();

        assertEquals(TableImpl.ROUND.BLINDS, tableController.getTable().getRound());
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CALL, 1));
        assertEquals(TableImpl.ROUND.FLOP, tableController.getTable().getRound());
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CHECK, 0));
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.BET, 300));
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.ALLIN, 198));
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CHECK, 0));
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CHECK, 0));
        int totalChips = 0;
        for(Pot pot : tableController.getTable().getPots()){
            totalChips += pot.getAmount();
            System.out.println(pot);
        }
        for(Player player : tableController.getTable().getPlayers()){
            totalChips += player.getChips();
        }
        System.out.println(totalChips);
        assertEquals(1200, totalChips);
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CHECK, 0));
        tableController.receiveTurn(new Turn(p = tableController.getTurnNotification().getPlayer(), Turn.PlayerAction.CHECK, 0));
    }
}
