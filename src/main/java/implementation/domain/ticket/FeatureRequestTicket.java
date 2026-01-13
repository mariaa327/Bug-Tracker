package implementation.domain.ticket;

import java.time.LocalDate;

public class FeatureRequestTicket extends Ticket {
    public FeatureRequestTicket(String title, BusinessPriority prio, LocalDate createdAt, String reportedBy) {
        super(TicketType.FEATURE_REQUEST, title, prio, createdAt, reportedBy);
    }
}
