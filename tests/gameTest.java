import poker.*;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class gameTest {
    public static void main(String[] args) {
        final TestController tableController = new TestController(new TableImpl());

        Thread controllerThread = new Thread(() -> {
            try {
                tableController.getTable().addPlayer(new Player("Bob", 250));
                tableController.getTable().addPlayer(new Player("Carl", 250));
                tableController.getTable().addPlayer(new Player("Joe", 250));
                tableController.startRound();
                System.out.println(tableController.getTable().getWinnerInfo());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        controllerThread.start();
        Scanner in = new Scanner(System.in);
        while (tableController.getTable().getRound().getRoundNum() < TableImpl.ROUND.INTERIM.getRoundNum()) {
            String[] input = in.nextLine().split(" ");
            Turn t = new Turn(tableController.getTable().getPlayerFromName(input[0]),
                    Turn.PlayerAction.valueOf(input[1].toUpperCase()),
                    ((input.length > 2) ? Integer.parseInt(input[2]) : 0));
            tableController.receiveTurn(t);
        }


    }
}
