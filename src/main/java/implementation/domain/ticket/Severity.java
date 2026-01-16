package implementation.domain.ticket;

public enum Severity {
    MINOR(1),
    MODERATE(2),
    SEVERE(3);

    private final int score;

    Severity(int score) {
        this.score = score;
    }

    public int score() {
        return score;
    }
}
