package implementation.factory;

import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.BusinessValue;
import implementation.domain.ticket.BugTicket;
import implementation.domain.ticket.CustomerDemand;
import implementation.domain.ticket.FeatureRequestTicket;
import implementation.domain.ticket.Frequency;
import implementation.domain.ticket.Severity;
import implementation.domain.ticket.Ticket;
import implementation.domain.ticket.TicketType;
import implementation.domain.ticket.UiFeedbackTicket;
import implementation.domain.user.ExpertiseArea;
import implementation.input.ParamsReportTicket;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

/**
 * central factory used to create ticket instances from input params
 * uses an internal ticketCreatorFactory strategy per TicketType
 */
public final class TicketFactory {

    private TicketFactory() { }

    private static final Map<TicketType, TicketCreatorFactory> CREATORS =
            new EnumMap<>(TicketType.class);

    static {
        CREATORS.put(TicketType.BUG, new BugCreator());
        CREATORS.put(TicketType.FEATURE_REQUEST, new FeatureRequestCreator());
        CREATORS.put(TicketType.UI_FEEDBACK, new UiFeedbackCreator());
    }

    /**
     * creates a ticket from the given timestamp and parsed report parameters
     * @param timestamp the creation timestamp (date string) used to parse
     *                  localDate
     * @param params the parsed parameters for report ticket
     * @return the created ticket instance
     * @throws IllegalArgumentException if the ticket type is not supported by this factory
     */
    public static Ticket createTicket(final String timestamp, final ParamsReportTicket params) {
        TicketType type = TicketType.valueOf(params.getType());
        LocalDate createdAt = LocalDate.parse(timestamp);

        BusinessPriority prio = BusinessPriority.valueOf(params.getBusinessPriority());
        String reportedBy = params.getReportedBy();
        ExpertiseArea exp = ExpertiseArea.valueOf(params.getExpertiseArea());

        TicketCreatorFactory creator = CREATORS.get(type);
        if (creator == null) {
            throw new IllegalArgumentException("Unknown ticket type: " + type);
        }

        return creator.create(createdAt, params, prio, reportedBy, exp);
    }

    private static final class BugCreator implements TicketCreatorFactory {

        /**
         * creates a BugTicket based on report params
         * @param createdAt the ticket creation date
         * @param p the parsed parameters for report ticket
         * @param prio the business priority
         * @param reportedBy the username that reported the ticket
         * @param exp the required expertise area
         * @return the created ticket
         */
        @Override
        public Ticket create(final LocalDate createdAt, final ParamsReportTicket p,
                             final BusinessPriority prio, final String reportedBy,
                             final ExpertiseArea exp) {

            Frequency freq = null;
            if (p.getFrequency() != null) {
                freq = Frequency.valueOf(p.getFrequency().trim().toUpperCase());
            }

            Severity sev = null;
            if (p.getSeverity() != null) {
                sev = Severity.valueOf(p.getSeverity().trim().toUpperCase());
            }

            return new BugTicket(p.getTitle(), prio, createdAt, reportedBy,
                    exp, null, freq, sev);
        }
    }

    private static final class FeatureRequestCreator implements TicketCreatorFactory {

        /**
         * creates a FeatureRequestTicket based on report params
         * @param createdAt the ticket creation date
         * @param p the parsed parameters for report ticket
         * @param prio the business priority
         * @param reportedBy the username that reported the ticket
         * @param exp the required expertise area
         * @return the created ticket
         */
        @Override
        public Ticket create(final LocalDate createdAt, final ParamsReportTicket p,
                             final BusinessPriority prio, final String reportedBy,
                             final ExpertiseArea exp) {

            BusinessValue bv = null;
            if (p.getBusinessValue() != null) {
                bv = BusinessValue.valueOf(p.getBusinessValue().trim().toUpperCase());
            }

            CustomerDemand cd = null;
            if (p.getCustomerDemand() != null) {
                cd = CustomerDemand.fromString(p.getCustomerDemand());
            }

            return new FeatureRequestTicket(p.getTitle(), prio, createdAt, reportedBy,
                    exp, null, bv, cd);
        }
    }

    private static final class UiFeedbackCreator implements TicketCreatorFactory {

        /**
         * creates a UiFeedbackTicket based on report params
         * @param createdAt the ticket creation date
         * @param p the parsed parameters for report ticket
         * @param prio the business priority
         * @param reportedBy the username that reported the ticket
         * @param exp the required expertise area
         * @return the created ticket
         */
        @Override
        public Ticket create(final LocalDate createdAt, final ParamsReportTicket p,
                             final BusinessPriority prio, final String reportedBy,
                             final ExpertiseArea exp) {

            BusinessValue bv = null;
            if (p.getBusinessValue() != null) {
                bv = BusinessValue.valueOf(p.getBusinessValue().trim().toUpperCase());
            }

            return new UiFeedbackTicket(p.getTitle(), prio, createdAt, reportedBy,
                    exp, null, bv, p.getUsabilityScore());
        }
    }
}
