package implementation.domain.ticket;

public enum BusinessPriority {
    LOW(1) {
        @Override
        public BusinessPriority bump() {
            return MEDIUM;
        }
    },
    MEDIUM(2) {
        @Override
        public BusinessPriority bump() {
            return HIGH;
        }
    },
    HIGH(3) {
        @Override
        public BusinessPriority bump() {
            return CRITICAL;
        }
    },
    CRITICAL(4) {
        @Override
        public BusinessPriority bump() {
            return CRITICAL;
        }
    };

    private final int score;

    /**
     * creates the business priority
     * @param score the priority score
     */
    BusinessPriority(final int score) {
        this.score = score;
    }

    /**
     * returns the priority score
     * @return the priority score
     */
    public int score() {
        return score;
    }

    /**
     * bumps the priority to the next level
     * @return the next priority level
     */
    public abstract BusinessPriority bump();
}
