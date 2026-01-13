package implementation.service;

import implementation.domain.flow.ProjectContext;

import java.time.LocalDate;

public class FlowService {
    private final ProjectContext ctx;

    public FlowService(ProjectContext ctx) {
        this.ctx = ctx;
    }

    public ProjectContext getContext() {
        return ctx;
    }

    public String validateTestingPhase(String currentTimestamp) {
        LocalDate start = ctx.getStartDate();
        if (start == null)
            return "Tickets can only be reported during testing phases.";

        LocalDate now = LocalDate.parse(currentTimestamp);
        long days = now.toEpochDay() - start.toEpochDay() + 1;

        if (days < 1 || days > 12) {
            return "Tickets can only be reported during testing phases.";
        }
        return null;
    }
}
