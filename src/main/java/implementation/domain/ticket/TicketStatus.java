package implementation.domain.ticket;

public enum TicketStatus {
    OPEN {
        @Override
        public TicketStatus next() {
            return IN_PROGRESS;
        }

        @Override
        public TicketStatus prev() {
            return OPEN;
        }
    },
    IN_PROGRESS {
        @Override
        public TicketStatus next() {
            return RESOLVED;
        }

        @Override
        public TicketStatus prev() {
            return OPEN;
        }
    },
    RESOLVED {
        @Override
        public TicketStatus next() {
            return CLOSED;
        }

        @Override
        public TicketStatus prev() {
            return IN_PROGRESS;
        }
    },
    CLOSED {
        @Override
        public TicketStatus next() {
            return CLOSED;
        }

        @Override
        public TicketStatus prev() {
            return RESOLVED;
        }
    };

    /**
     * returns the next status in the workflow
     * @return the next status
     */
    public abstract TicketStatus next();

    /**
     * returns the previous status in the workflow
     * @return the previous status
     */
    public abstract TicketStatus prev();
}
