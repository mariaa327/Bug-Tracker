package implementation.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.Ticket;
import implementation.domain.ticket.TicketType;

import java.util.ArrayList;
import java.util.List;

/**
 * generates the "customer impact" metrics report based on a list of tickets
 * this strategy filters the input tickets to only those eligible for
 * customer impact
 * then aggregates counts by type and priority and computes the average impact score per type
 */
public class CustomerImpactReportStrategy implements ReportStrategy {

    /**
     * builds the customer impact report as a JSON object
     * @param tickets the input list of tickets to analyze
     * @param mapper the ObjectMapper used to create JSON nodes
     * @return the generated report as an ObjectNode
     */
    @Override
    public ObjectNode generate(final List<Ticket> tickets, final ObjectMapper mapper) {
        final List<Ticket> eligible = filterEligibleTickets(tickets);

        final ObjectNode report = mapper.createObjectNode();
        report.put("totalTickets", eligible.size());

        report.set("ticketsByType", buildTicketsByType(eligible, mapper));
        report.set("ticketsByPriority", buildTicketsByPriority(eligible, mapper));
        report.set("customerImpactByType", buildImpactByType(eligible, mapper));

        return report;
    }

    /**
     * filters the input tickets to keep only those that are eligible for
     * customer impact
     * @param tickets the input list of tickets (may be null)
     * @return a list containing only eligible tickets (never null)
     */
    private List<Ticket> filterEligibleTickets(final List<Ticket> tickets) {
        final List<Ticket> eligible = new ArrayList<>();
        if (tickets == null) {
            return eligible;
        }

        for (Ticket t : tickets) {
            if (t == null) {
                continue;
            }

            if (t.eligibleForCustomerImpact()) {
                eligible.add(t);
            }
        }

        return eligible;
    }

    /**
     * computes how many eligible tickets exist for each TicketType
     * @param eligible the already filtered list of eligible tickets
     * @param mapper the ObjectMapper used to create JSON nodes
     * @return a JSON object mapping ticket type name to count
     */
    private ObjectNode buildTicketsByType(final List<Ticket> eligible, final ObjectMapper mapper) {
        final ObjectNode byType = mapper.createObjectNode();

        for (TicketType type : TicketType.values()) {
            long count = 0;

            for (Ticket t : eligible) {
                if (t.getType() == type) {
                    count++;
                }
            }

            byType.put(type.name(), count);
        }

        return byType;
    }

    /**
     * computes how many eligible tickets exist for each BusinessPriority
     * @param eligible the already filtered list of eligible tickets
     * @param mapper the ObjectMapper used to create JSON nodes
     * @return a JSON object mapping priority name to count
     */
    private ObjectNode buildTicketsByPriority(final List<Ticket> eligible,
                                              final ObjectMapper mapper) {
        final ObjectNode byPriority = mapper.createObjectNode();

        for (BusinessPriority p : BusinessPriority.values()) {
            long count = 0;

            for (Ticket t : eligible) {
                if (t.getBusinessPriority() == p) {
                    count++;
                }
            }

            byPriority.put(p.name(), count);
        }

        return byPriority;
    }

    /**
     * computes the average customer impact score for each ticketType
     * across the eligible tickets, rounding each average to 2 decimals
     * @param eligible the already filtered list of eligible tickets
     * @param mapper the ObjectMapper used to create JSON nodes
     * @return a JSON object mapping ticket type name to rounded average impact score
     */
    private ObjectNode buildImpactByType(final List<Ticket> eligible, final ObjectMapper mapper) {
        final ObjectNode impactByType = mapper.createObjectNode();

        for (TicketType type : TicketType.values()) {
            double sum = 0.0;
            int count = 0;

            for (Ticket t : eligible) {
                if (t.getType() == type) {
                    sum += t.customerImpactScore();
                    count++;
                }
            }

            final double avg = (count == 0) ? 0.0 : (sum / count);
            impactByType.put(type.name(), round2(avg));
        }

        return impactByType;
    }

    /**
     * rounds a floating-point value to 2 decimal places
     * @param v the value to round
     * @return the value rounded to 2 decimals
     */
    private double round2(final double v) {
        return Math.round(v * HUNDRED) / HUNDRED;
    }

    /**
     * scale factor used for rounding to 2 decimals
     */
    private static final double HUNDRED = 100.0;
}
