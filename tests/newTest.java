import org.junit.Test;
import poker.Player;
import poker.TableImpl;
import poker.Turn;
import poker.TurnNotification;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class newTest {

    @Test
    public void gameLogicTest() throws InterruptedException {
        final TestController tableController = new TestController(new TableImpl());

        Thread controllerThread = new Thread(() -> {
            try {
                tableController.getTable().addPlayer(new Player("Bob", 250));
                tableController.getTable().addPlayer(new Player("Carl", 250));
                tableController.getTable().addPlayer(new Player("Joe", 250));
                tableController.getTable().addPlayer(new Player("Sneed", 250));
                tableController.getTable().addPlayer(new Player("Chuck", 250));
                tableController.startRound();
                System.out.println(tableController.getTable().getWinnerInfo());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        controllerThread.start();

        while(tableController.getTable().getPlayers().size() < 3) {
            Thread.sleep(100);
        }

        List<Player> p_list = tableController.getTable().getPlayers();

        assertTrue(p_list.get(1).isDealer());
        assertEquals(p_list.get(4), tableController.getTurnNotification().getPlayer());
        System.out.println(Arrays.toString(tableController.getTable().activePlayers().stream().map(Player::getName).toArray()));

        tableController.receiveTurn(new Turn(p_list.get(4), Turn.PlayerAction.FOLD, 0));
        Thread.sleep(400);
        System.out.println(Arrays.toString(tableController.getTable().activePlayers().stream().map(Player::getName).toArray()));

        assertEquals(p_list.get(0).getName(), tableController.getTurnNotification().getPlayer().getName());

        tableController.receiveTurn(new Turn(p_list.get(0), Turn.PlayerAction.FOLD, 0));
        Thread.sleep(400);
        System.out.println(Arrays.toString(tableController.getTable().activePlayers().stream().map(Player::getName).toArray()));
        assertEquals(p_list.get(1), tableController.getTurnNotification().getPlayer());

    }

    @Test
    public void fastFoldTest() throws InterruptedException {
        final TestController tableController = new TestController(new TableImpl());

        Thread controllerThread = new Thread(() -> {
            try {
                tableController.getTable().addPlayer(new Player("Bob", 250));
                tableController.getTable().addPlayer(new Player("Carl", 250));
                tableController.startRound();
                System.out.println(tableController.getTable().getWinnerInfo());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        controllerThread.start();
        while(tableController.getTable().getPlayers().size() < 2) {
            Thread.sleep(100);
        }
        List<Player> p_list = tableController.getTable().getPlayers();
        assertEquals(p_list.get(0), tableController.getTurnNotification().getPlayer());
        tableController.receiveTurn(new Turn(p_list.get(1), Turn.PlayerAction.FOLD, 0));
        Thread.sleep(400);
        System.out.println(Arrays.toString(tableController.getTable().activePlayers().stream().map(Player::getName).toArray()));
    }
}
