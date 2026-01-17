package implementation.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.domain.ticket.Ticket;

import java.util.List;

public interface ReportStrategy {
    ObjectNode generate(List<Ticket> tickets, ObjectMapper mapper);
}
