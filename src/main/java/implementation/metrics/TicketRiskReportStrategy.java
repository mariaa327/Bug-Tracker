package implementation.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.domain.ticket.*;

import java.util.List;

public class TicketRiskReportStrategy implements ReportStrategy {

    @Override
    public ObjectNode generate(List<Ticket> tickets, ObjectMapper mapper) {
        List<Ticket> eligible = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.OPEN || t.getStatus() == TicketStatus.IN_PROGRESS)
                .toList();

        ObjectNode report = mapper.createObjectNode();
        report.put("totalTickets", eligible.size());

        ObjectNode byType = mapper.createObjectNode();
        for (TicketType type : TicketType.values()) {
            long c = eligible.stream().filter(t -> t.getType() == type).count();
            byType.put(type.name(), c);
        }
        report.set("ticketsByType", byType);

        ObjectNode byPriority = mapper.createObjectNode();
        for (BusinessPriority p : BusinessPriority.values()) {
            long c = eligible.stream().filter(t -> t.getBusinessPriority() == p).count();
            byPriority.put(p.name(), c);
        }
        report.set("ticketsByPriority", byPriority);

        ObjectNode riskByType = mapper.createObjectNode();
        for (TicketType type : TicketType.values()) {
            double avg = eligible.stream()
                    .filter(t -> t.getType() == type)
                    .mapToDouble(Ticket::riskPercent)
                    .average()
                    .orElse(0.0);

            riskByType.put(type.name(), RiskLevel.fromPercent(avg).name());
        }
        report.set("riskByType", riskByType);

        return report;
    }
}
