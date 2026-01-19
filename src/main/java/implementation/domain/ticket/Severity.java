package implementation.domain.ticket;

public enum Severity {
    MINOR(1),
    MODERATE(2),
    SEVERE(3);

    private final int score;

    /**
     * creates the severity
     * @param score the severity score
     */
    Severity(final int score) {
        this.score = score;
    }

    /**
     * returns the severity score
     * @return the severity score
     */
    public int score() {
        return score;
    }
}
