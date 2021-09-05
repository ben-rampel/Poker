package pokertest;

import org.junit.Test;
import poker.Card;
import poker.Player;
import webapp.Lobby;
import webapp.LobbyImpl;
import webapp.TableController;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class allInTest {
    @Test
    public void test1() throws InterruptedException {
        TestPlayer andrew, ben, chuck;
        Lobby lobby = new LobbyImpl(600);
        TestPlayer[] players = {
                andrew = new TestPlayer("andrew", 50, lobby),
                ben = new TestPlayer("ben", 900, lobby),
                chuck = new TestPlayer("chuck", 250, lobby)
        };
        lobby.start();
        for (Player p : players){
            lobby.addPlayer(p);
            Thread.sleep(200);
        }
        andrew.call(1);
        ben.fold();
        lobby.awaitWinner();
        assertEquals(TableController.State.DONE, lobby.getControllerState());
        Thread.sleep(650);
        assertEquals(TableController.State.READY, lobby.getControllerState());
        System.out.println(andrew.getChips());
        chuck.call(2);
        andrew.call(1);
        ben.sendBet(240);
        chuck.call(240);
        andrew.allin();
        lobby.awaitReady();
        assertEquals((50+2)*3, lobby.getState().getPot());
        assertEquals((240-50)*2, lobby.getState().getSidePots()[0].getAmount());
        ben.check();
        chuck.sendBet(chuck.getChips());
        ben.raise(69);
        Thread.sleep(200);
        assertTrue(chuck.isAllIn());
        ben.check();
        System.out.println(lobby.getState().getTurnNotification());
        lobby.awaitWinner();
        System.out.println(Arrays.toString(lobby.getState().getSidePots()));
        System.out.println(ben.getChips() + " " + chuck.getChips() + " " + andrew.getChips());
        assertEquals(ben.getChips()+chuck.getChips()+andrew.getChips(), 1200);
    }
    @Test
    public void immediateAllInTest() throws InterruptedException {
        TestPlayer andrew, ben;
        Lobby lobby = new LobbyImpl(600);
        TestPlayer[] players = {
                andrew = new TestPlayer("andrew", 250, lobby),
                ben = new TestPlayer("ben", 250, lobby),
        };
        lobby.start();
        for (Player p : players){
            lobby.addPlayer(p);
            Thread.sleep(200);
        }
        andrew.call(1);
        ben.sendBet(248);
        andrew.call(248);
        lobby.awaitWinner();
        System.out.println(andrew.getChips() + "  " + ben.getChips());
        assertTrue(andrew.getChips() == 500 || ben.getChips() == 500);
    }
}
