package implementation.domain.ticket;

public enum BusinessValue {
    S(1),
    M(3),
    L(6),
    XL(10);

    private final int score;

    /**
     * creates the business value
     * @param score the value score
     */
    BusinessValue(final int score) {
        this.score = score;
    }

    /**
     * returns the value score
     * @return the value score
     */
    public int score() {
        return score;
    }
}
