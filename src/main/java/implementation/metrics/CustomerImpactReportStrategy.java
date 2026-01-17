package implementation.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.Ticket;
import implementation.domain.ticket.TicketType;

import java.util.List;

public class CustomerImpactReportStrategy implements ReportStrategy {

    @Override
    public ObjectNode generate(List<Ticket> tickets, ObjectMapper mapper) {
        List<Ticket> eligible = tickets.stream()
                .filter(Ticket::eligibleForCustomerImpact)
                .toList();

        ObjectNode report = mapper.createObjectNode();
        report.put("totalTickets", eligible.size());

        ObjectNode byType = mapper.createObjectNode();
        for (TicketType t : TicketType.values()) {
            long c = eligible.stream().filter(x -> x.getType() == t).count();
            byType.put(t.name(), c);
        }
        report.set("ticketsByType", byType);

        ObjectNode byPriority = mapper.createObjectNode();
        for (BusinessPriority p : BusinessPriority.values()) {
            long c = eligible.stream().filter(x -> x.getBusinessPriority() == p).count();
            byPriority.put(p.name(), c);
        }
        report.set("ticketsByPriority", byPriority);

        ObjectNode impactByType = mapper.createObjectNode();
        for (TicketType t : TicketType.values()) {
            double avg = eligible.stream()
                    .filter(x -> x.getType() == t)
                    .mapToDouble(Ticket::customerImpactScore)
                    .average()
                    .orElse(0.0);

            impactByType.put(t.name(), round2(avg));
        }
        report.set("customerImpactByType", impactByType);

        return report;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
