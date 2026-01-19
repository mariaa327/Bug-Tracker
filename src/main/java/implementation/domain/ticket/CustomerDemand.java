package implementation.domain.ticket;

public enum CustomerDemand {
    LOW(1),
    MEDIUM(3),
    HIGH(6),
    VERY_HIGH(10);

    private final int score;

    /**
     * creates the customer demand
     * @param score the demand score
     */
    CustomerDemand(final int score) {
        this.score = score;
    }

    /**
     * returns the demand score
     * @return the demand score
     */
    public int score() {
        return score;
    }

    /**
     * parses a customer demand from string
     * @param s the input string
     * @return the parsed customer demand or null if input is null
     */
    public static CustomerDemand fromString(final String s) {
        if (s == null) {
            return null;
        }

        String v = s.trim().toUpperCase();
        if ("CRITICAL".equals(v)) {
            return VERY_HIGH;
        }

        return CustomerDemand.valueOf(v);
    }
}
