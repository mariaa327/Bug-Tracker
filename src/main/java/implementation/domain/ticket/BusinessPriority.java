package implementation.domain.ticket;

public enum BusinessPriority {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW;

    public BusinessPriority bump() {
        return switch (this) {
            case LOW -> MEDIUM;
            case MEDIUM -> HIGH;
            case HIGH -> CRITICAL;
            case CRITICAL -> CRITICAL;
        };
    }

    public int score() {
        return switch (this) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };
    }
}
