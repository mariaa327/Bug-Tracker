package implementation.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.domain.ticket.Ticket;

import java.util.List;

/**
 * strategy interface for generating JSON-based reports from a collection of
 * tickets
 * implementations are responsible for defining the report schema and
 * computing the
 * aggregated values based on the provided tickets
 */
public interface ReportStrategy {
    /**
     * generates a report as a JSON object from the given tickets
     * @param tickets the input list of tickets used to compute the report
     * @param mapper the ObjectMapper used to create JSON nodes
     * @return the generated report as an ObjectNode
     */
    ObjectNode generate(List<Ticket> tickets, ObjectMapper mapper);
}
