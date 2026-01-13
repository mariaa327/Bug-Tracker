package implementation.domain.ticket;

import java.time.LocalDate;

public class UiFeedbackTicket extends Ticket {
    public UiFeedbackTicket(String title, BusinessPriority prio, LocalDate createdAt, String reportedBy) {
        super(TicketType.UI_FEEDBACK, title, prio, createdAt, reportedBy);
    }
}
