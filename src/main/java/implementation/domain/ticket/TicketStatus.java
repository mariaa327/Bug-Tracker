package implementation.domain.ticket;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    public TicketStatus next() {
        return switch (this) {
            case OPEN -> IN_PROGRESS;
            case IN_PROGRESS -> RESOLVED;
            case RESOLVED -> CLOSED;
            case CLOSED -> CLOSED;
        };
    }

    public TicketStatus prev() {
        return switch (this) {
            case OPEN -> OPEN;
            case IN_PROGRESS -> OPEN;
            case RESOLVED -> IN_PROGRESS;
            case CLOSED -> RESOLVED;
        };
    }
}
