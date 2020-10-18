import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import poker.Table;
import poker.Turn;
import poker.TurnNotification;
import webapp.TableController;

import java.util.concurrent.Future;

@Component
public class TestController extends TableController {

    @Autowired
    public TestController(Table table) {
        super(table);
    }

    @Override
    public Future<Turn> sendTurnNotification(TurnNotification turnNotification) {
        System.out.println(turnNotification);
        return super.sendTurnNotification(turnNotification);
    }

    public TurnNotification getTurnNotification(){
        return this.turnNotification;
    }
}
