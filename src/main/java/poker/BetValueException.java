package poker;

public class BetValueException extends RuntimeException {
    private final Player player;

    public BetValueException(Player p) {
        super("Cannot bet more chips than you have");
        player = p;
    }

    public Player getPlayer() {
        return player;
    }
}
