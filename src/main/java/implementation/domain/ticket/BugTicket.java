package implementation.domain.ticket;

import java.time.LocalDate;

public class BugTicket extends Ticket {
    public BugTicket(String title, BusinessPriority prio, LocalDate createdAt, String reportedBy) {
        super(TicketType.BUG, title, prio, createdAt, reportedBy);
    }
}
