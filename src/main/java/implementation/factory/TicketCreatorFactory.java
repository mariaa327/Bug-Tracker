package implementation.factory;

import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.Ticket;
import implementation.domain.user.ExpertiseArea;
import implementation.input.ParamsReportTicket;

import java.time.LocalDate;

/**
 * factory interface used to create tickets based on common input parameters
 */
public interface TicketCreatorFactory {
    /**
     * creates a ticket instance using the provided creation date, parsed parameters and context
     * @param createdAt the ticket creation date
     * @param params the parsed parameters for the report ticket command
     * @param prio the business priority
     * @param reportedBy the username that reported the ticket
     * @param exp the required expertise area (can be null depending on ticket type)
     * @return the created ticket
     */
    Ticket create(LocalDate createdAt,
                  ParamsReportTicket params,
                  BusinessPriority prio,
                  String reportedBy,
                  ExpertiseArea exp);
}
