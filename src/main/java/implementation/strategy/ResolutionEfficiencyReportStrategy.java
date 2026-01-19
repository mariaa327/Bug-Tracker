package implementation.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.Ticket;
import implementation.domain.ticket.TicketStatus;
import implementation.domain.ticket.TicketType;

import java.util.ArrayList;
import java.util.List;

/**
 * generates a report over resolved/closed tickets, aggregating totals and
 * average resolution efficiency
 */
public class ResolutionEfficiencyReportStrategy implements ReportStrategy {
    private static final double TWO_DECIMAL_SCALE = 100.0;

    /**
     * builds the report JSON
     * @param tickets list of tickets to analyze
     * @param mapper  Jackson mapper used to create JSON nodes
     * @return report as an objectnode
     */
    @Override
    public ObjectNode generate(final List<Ticket> tickets,
                               final ObjectMapper mapper) {
        List<Ticket> eligible = filterEligibleTickets(tickets);

        ObjectNode report = mapper.createObjectNode();
        report.put("totalTickets", eligible.size());

        report.set("ticketsByType", buildTicketsByType(eligible, mapper));
        report.set("ticketsByPriority", buildTicketsByPriority(eligible, mapper));
        report.set("efficiencyByType", buildEfficiencyByType(eligible, mapper));

        return report;
    }

    /**
     * filters the input tickets to keep only those eligible for the risk report
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
            if (s == TicketStatus.RESOLVED || s == TicketStatus.CLOSED) {
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
     * computes the risk-related metric per TicketType for the eligible tickets
     * @param eligible the already filtered list of eligible tickets
     * @param mapper the ObjectMapper used to create JSON nodes
     * @return a JSON object mapping ticket type name to the computed risk metric
     */
    private ObjectNode buildEfficiencyByType(final List<Ticket> eligible,
                                             final ObjectMapper mapper) {
        ObjectNode effByType = mapper.createObjectNode();

        for (TicketType type : TicketType.values()) {
            double sum = 0.0;
            int count = 0;

            for (Ticket t : eligible) {
                if (t.getType() == type) {
                    sum += t.efficiencyScore();
                    count++;
                }
            }

            double avg = (count == 0) ? 0.0 : (sum / count);
            effByType.put(type.name(), round2(avg));
        }

        return effByType;
    }

    private double round2(final double v) {
        return Math.round(v * TWO_DECIMAL_SCALE) / TWO_DECIMAL_SCALE;
    }
}
