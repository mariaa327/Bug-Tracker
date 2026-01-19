package implementation.domain.ticket;

public enum Frequency {
    RARE(1),
    OCCASIONAL(2),
    FREQUENT(3),
    ALWAYS(4);

    private final int score;

    /**
     * creates the frequency
     * @param score the frequency score
     */
    Frequency(final int score) {
        this.score = score;
    }

    /**
     * returns the frequency score
     * @return the frequency score
     */
    public int score() {
        return score;
    }
}
