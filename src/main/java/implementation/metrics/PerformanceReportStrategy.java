package implementation.metrics;

import implementation.service.TicketService;

import java.util.List;
import java.util.Map;

public class PerformanceReportStrategy {

    public List<Map<String, Object>> generate(String managerUsername,
                                              String timestamp,
                                              TicketService ticketService) {
        return ticketService.generatePerformanceReport(managerUsername, timestamp);
    }
}
