package poker.handComparators;

import poker.Card;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class RankOrderComparator implements Comparator<Card> {
    //Comparator to facilitate comparison of card connectedness
    public int compare(Card a, Card b) {
        List<Card.Rank> order = Arrays.asList(Card.Rank.values());
        return order.indexOf(b.getRank()) - order.indexOf(a.getRank());
    }
}
