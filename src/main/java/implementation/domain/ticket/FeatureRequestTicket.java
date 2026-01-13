package implementation.domain.ticket;

import implementation.domain.user.ExpertiseArea;

import java.time.LocalDate;

public class FeatureRequestTicket extends Ticket {
    public FeatureRequestTicket() { super(null, null, null,
            null, null, null, null); }

    public FeatureRequestTicket(String title,
                                BusinessPriority businessPriority,
                                LocalDate createdAt,
                                String reportedBy,
                                ExpertiseArea expertiseArea,
                                String description) {
        super(TicketType.FEATURE_REQUEST, title, businessPriority, createdAt, reportedBy, expertiseArea, description);
    }
}
