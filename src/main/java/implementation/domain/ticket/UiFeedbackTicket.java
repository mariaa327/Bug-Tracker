package implementation.domain.ticket;

import implementation.domain.user.ExpertiseArea;

import java.time.LocalDate;

public class UiFeedbackTicket extends Ticket {
    public UiFeedbackTicket() { super(null, null, null,
            null, null, null, null); }

    public UiFeedbackTicket(String title,
                            BusinessPriority businessPriority,
                            LocalDate createdAt,
                            String reportedBy,
                            ExpertiseArea expertiseArea,
                            String description) {
        super(TicketType.UI_FEEDBACK, title, businessPriority, createdAt,
                reportedBy, expertiseArea, description);
    }
}
