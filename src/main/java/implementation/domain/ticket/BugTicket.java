package implementation.domain.ticket;

import implementation.domain.user.ExpertiseArea;

import java.time.LocalDate;

public class BugTicket extends Ticket {
    public BugTicket() { super(null, null, null,
            null, null, null, null); }

    public BugTicket(String title,
                     BusinessPriority businessPriority,
                     LocalDate createdAt,
                     String reportedBy,
                     ExpertiseArea expertiseArea,
                     String description) {
        super(TicketType.BUG, title, businessPriority, createdAt, reportedBy, expertiseArea, description);
    }
}
