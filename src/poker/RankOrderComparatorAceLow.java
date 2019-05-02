package poker;

import java.util.Arrays;
import java.util.List;

public class RankOrderComparatorAceLow extends RankOrderComparator {
    @Override
    public int compare(Card a, Card b) {
        List<Card.Rank> order = Arrays.asList(Card.Rank.values());
        if (b.getRank() == Card.Rank.ace && a.getRank() == Card.Rank.two) {
            return -1;
        }
        return order.indexOf(b.getRank()) - order.indexOf(a.getRank());
    }
}
