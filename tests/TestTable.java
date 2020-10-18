import poker.Card;
import poker.TableImpl;

import java.util.List;


public class TestTable extends TableImpl {
    public void changeCommonCard(List<Card> cards) {
        this.commonCards = cards;
    }
}
