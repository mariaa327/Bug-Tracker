package implementation.domain.ticket;

public enum Frequency {
    RARE(1),
    OCCASIONAL(2),
    FREQUENT(3),
    ALWAYS(4);

    private final int score;

    Frequency(int score) {
        this.score = score;
    }

    public int score() {
        return score;
    }
}
