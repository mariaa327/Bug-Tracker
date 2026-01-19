package implementation.strategy;

import implementation.service.TicketService;

import java.util.List;
import java.util.Map;

/**
 * generates a performance report for a given manager and timestamp
 * if this class is subclassed overriding
 * should preserve the contract of delegating to TicketService and returning
 * the same report structure
 */
public class PerformanceReportStrategy {

    /**
     * generates the performance report by delegating the computation to
     * TicketService
     * @param managerUsername the username of the manager for which the report is generated
     * @param timestamp the timestamp used as reference when generating the report
     * @param ticketService the service responsible for computing the performance report
     * @return the generated performance report as a list of maps (output-ready structure)
     */
    public List<Map<String, Object>> generate(final String managerUsername,
                                              final String timestamp,
                                              final TicketService ticketService) {
        return ticketService.generatePerformanceReport(managerUsername, timestamp);
    }
}
