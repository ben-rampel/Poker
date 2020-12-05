package poker;

import java.util.*;

import static poker.utils.isFlush;
import static poker.utils.isStraight;

public class Hand implements Comparable<Hand> {
    public enum handName {
        highCard(0, "A "), pair(1, "A pair"), twoPair(2, "Two Pair"), trips(3, "Three of a Kind"), straight(4, "Straight"),
        flush(5, "Flush"), fullHouse(6, "Full House"), quads(7, "Four of a Kind"), straightFlush(8, "Straight Flush"), royalFlush(9, "Royal Flush");
        private final int rank;
        private final String name;

        handName(int rank, String name) {
            this.rank = rank;
            this.name = name;
        }

        public int getRank() {
            return rank;
        }

        public String getName() {
            return name;
        }
    }

    private final List<Card> cards;
    private final handName rank;
    private final List<Card.Rank> coefficients;

    public Hand(List<Card> cardList) {
        cards = new ArrayList<>(cardList);
        Collections.sort(cards);
        rank = determineRank();
        coefficients = determineCoefficients();
    }

    private handName determineRank() {
        Map<Card.Rank, Integer> rankMap = new HashMap<>();
        for (Card c : cards) {
            rankMap.putIfAbsent(c.getRank(), 0);
            rankMap.put(c.getRank(), rankMap.get(c.getRank()) + 1);
        }
        boolean straight = isStraight(cards);
        boolean flush = isFlush(cards);
        if (straight && flush && cards.get(4).getRank() == Card.Rank.ace) {
            return handName.royalFlush;
        }
        if (straight && flush) return handName.straightFlush;
        if (rankMap.containsValue(4)) return handName.quads;
        if (rankMap.containsValue(3) && rankMap.containsValue(2)) return handName.fullHouse;
        if (flush) return handName.flush;
        if (straight) return handName.straight;
        if (rankMap.containsValue(3)) return handName.trips;
        if (Collections.frequency(rankMap.values(), 2) == 2) return handName.twoPair;
        if (rankMap.containsValue(2)) return handName.pair;
        return handName.highCard;
    }

    private List<Card.Rank> determineCoefficients() {
        if (rank == handName.straight || rank == handName.straightFlush || rank == handName.flush) {
            return new LinkedList<>(Collections.singletonList(getHighCard()));
        }
        List<Card.Rank> list = new LinkedList<>();
        List<Card.Rank> cardsAsRanksList = new LinkedList<>();
        for (Card c : cards) {
            cardsAsRanksList.add(c.getRank());
        }
        if (rank == handName.fullHouse) {
            for (Card.Rank c : cardsAsRanksList)
                if (Collections.frequency(cardsAsRanksList, c) == 3) {
                    list.add(c);
                    break;
                }
            for (Card.Rank c : cardsAsRanksList)
                if (Collections.frequency(cardsAsRanksList, c) == 2) {
                    list.add(c);
                    break;
                }
            return list;
        }
        if (rank == handName.quads) {
            for (Card.Rank c : cardsAsRanksList)
                if (Collections.frequency(cardsAsRanksList, c) == 4) {
                    list.add(c);
                    break;
                }
            for (Card.Rank c : cardsAsRanksList)
                if (Collections.frequency(cardsAsRanksList, c) == 1) {
                    list.add(c);
                    break;
                }
            return list;
        }
        if (rank == handName.trips) {
            for (Card.Rank c : cardsAsRanksList)
                if (Collections.frequency(cardsAsRanksList, c) == 3) {
                    list.add(c);
                    break;
                }
            for (Card.Rank c : cardsAsRanksList)
                if (Collections.frequency(cardsAsRanksList, c) == 1) {
                    list.add(c);
                }
            return list;
        }
        if (rank == handName.twoPair) {
            for (Card.Rank c : cardsAsRanksList)
                if (Collections.frequency(cardsAsRanksList, c) == 2 && !list.contains(c)) {
                    list.add(c);
                }
            for (Card.Rank c : cardsAsRanksList)
                if (Collections.frequency(cardsAsRanksList, c) == 1 && !list.contains(c)) {
                    list.add(c);
                }
            return list;
        }
        if (rank == handName.pair) {
            for (Card.Rank c : cardsAsRanksList)
                if (Collections.frequency(cardsAsRanksList, c) == 2) {
                    list.add(c);
                    break;
                }
            for (Card.Rank c : cardsAsRanksList)
                if (Collections.frequency(cardsAsRanksList, c) == 1 && !list.contains(c)) {
                    list.add(c);
                }
            return list;
        }
        if (rank == handName.highCard) {
            for (Card c : cards) {
                list.add(c.getRank());
            }
            return list;
        }
        return list;
    }

    public List<Card> getCards() {
        return cards;
    }

    public handName getHandName() {
        return rank;
    }

    public Card.Rank getHighCard() {
        if (rank == handName.straight && cards.get(0).getRank() == Card.Rank.two) {
            return Card.Rank.five;
        }
        return cards.get(4).getRank();
    }

    public int getValue() {
        if (rank == handName.royalFlush) {
            return 13 * rank.getRank();
        }
        return 13 * rank.getRank() + coefficients.get(0).index();
    }

    public int compareTo(Hand other) {
        if (getValue() != other.getValue()) {
            return this.getValue() - other.getValue();
        } else {
            for (int i = 0; i < coefficients.size(); i++) {
                if (coefficients.get(i).compareTo(other.coefficients.get(i)) != 0) {
                    return coefficients.get(i).compareTo(other.coefficients.get(i));
                }
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        if (rank == handName.highCard) {
            return "High card of " + getHighCard();
        }
        if (rank == handName.royalFlush || rank == handName.straightFlush || rank == handName.straight || rank == handName.flush) {
            return rank.name + ((coefficients.size() != 0) ? ", " + coefficients.get(0) + " high" : "");
        }
        if (rank == handName.quads || rank == handName.trips || rank == handName.pair) {
            return rank.getName() + " of " + coefficients.get(0);
        }
        if (rank == handName.twoPair) {
            return "Two pair: " + coefficients.get(0) + "s and " + coefficients.get(1) + "s";
        }
        return "Full House: " + coefficients.get(0) + "s full of " + coefficients.get(1);
    }

}
