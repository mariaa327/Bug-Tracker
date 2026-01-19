package implementation.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.RiskLevel;
import implementation.domain.ticket.Ticket;
import implementation.domain.ticket.TicketStatus;
import implementation.domain.ticket.TicketType;

import java.util.ArrayList;
import java.util.List;

/**
 * generates a "ticket risk" report based on a list of tickets
 * the report includes only tickets with status OPEN or IN_PROGRESS and
 * aggregates:
 * total eligible tickets, counts by type and priority, and a risk level per type
 */
public class TicketRiskReportStrategy implements ReportStrategy {

    /**
     * builds the ticket risk report as a JSON object
     * if this class is subclassed, overriding this method should preserve
     * the same
     * output schema and eligibility rules (OPEN / IN_PROGRESS) to remain compatible
     * with the expected output format
     * @param tickets the input list of tickets to analyze
     * @param mapper the ObjectMapper used to create JSON nodes
     * @return the generated report as an ObjectNode
     */
    @Override
    public ObjectNode generate(final List<Ticket> tickets,
                               final ObjectMapper mapper) {
        List<Ticket> eligible = filterEligibleTickets(tickets);

        ObjectNode report = mapper.createObjectNode();
        report.put("totalTickets", eligible.size());

        report.set("ticketsByType", buildTicketsByType(eligible, mapper));
        report.set("ticketsByPriority", buildTicketsByPriority(eligible, mapper));
        report.set("riskByType", buildRiskByType(eligible, mapper));

        return report;
    }

    /**
     * filters the input tickets to keep only those with status OPEN or
     * IN_PROGRESS
     * @param tickets the input list of tickets (may be null)
     * @return a list containing only eligible tickets (never null)
     */
    private List<Ticket> filterEligibleTickets(final List<Ticket> tickets) {
        List<Ticket> eligible = new ArrayList<>();
        if (tickets == null) {
            return eligible;
        }

        for (Ticket t : tickets) {
            if (t == null) {
                continue;
            }

            TicketStatus s = t.getStatus();
            if (s == TicketStatus.OPEN || s == TicketStatus.IN_PROGRESS) {
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
    private ObjectNode buildTicketsByType(final List<Ticket> eligible,
                                          final ObjectMapper mapper) {
        ObjectNode byType = mapper.createObjectNode();

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
        ObjectNode byPriority = mapper.createObjectNode();

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
     * computes the risk level per TicketType for the eligible tickets
     * @param eligible the already filtered list of eligible tickets
     * @param mapper the ObjectMapper used to create JSON nodes
     * @return a JSON object mapping ticket type name to risk level name
     */
    private ObjectNode buildRiskByType(final List<Ticket> eligible,
                                       final ObjectMapper mapper) {
        ObjectNode riskByType = mapper.createObjectNode();

        for (TicketType type : TicketType.values()) {
            double sum = 0.0;
            int count = 0;

            for (Ticket t : eligible) {
                if (t.getType() == type) {
                    sum += t.riskPercent();
                    count++;
                }
            }

            double avg = (count == 0) ? 0.0 : (sum / count);

            RiskLevel level = RiskLevel.fromPercent(avg);
            riskByType.put(type.name(), level.name());
        }

        return riskByType;
    }
}
