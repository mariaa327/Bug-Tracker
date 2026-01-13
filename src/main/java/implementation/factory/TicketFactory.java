package implementation.factory;

import implementation.domain.ticket.*;
import implementation.domain.user.ExpertiseArea;
import implementation.dto.input.ParamsReportTicket;

import java.time.LocalDate;

public class TicketFactory {
    public static Ticket createTicket(ParamsReportTicket params) {
        TicketType type = TicketType.valueOf(params.getType());
        String title = params.getTitle();
        BusinessPriority prio = BusinessPriority.valueOf(params.getBusinessPriority());

        String reportedBy = (params.getReportedBy() == null) ? "" : params.getReportedBy();
        ExpertiseArea exp = ExpertiseArea.valueOf(params.getExpertiseArea());

        LocalDate now = LocalDate.now();

        return switch (type) {
            case BUG -> new BugTicket(title, prio, now, reportedBy, exp, null);
            case FEATURE_REQUEST -> new FeatureRequestTicket(title, prio, now, reportedBy, exp, null);
            case UI_FEEDBACK -> new UiFeedbackTicket(title, prio, now, reportedBy, exp, null);
        };
    }
}
