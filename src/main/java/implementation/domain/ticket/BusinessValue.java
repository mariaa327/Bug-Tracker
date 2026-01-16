package implementation.domain.ticket;

public enum BusinessValue {
    S(1),
    M(3),
    L(6),
    XL(10);

    private final int score;

    BusinessValue(int score) {
        this.score = score;
    }

    public int score() {
        return score;
    }
}
