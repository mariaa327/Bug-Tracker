package implementation.service;

import implementation.domain.flow.ProjectContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * handles project flow rules such as validating the testing phase window
 */
public final class FlowService {

    private static final int TESTING_PHASE_DAYS = 12;

    private final ProjectContext ctx;

    /**
     * creates a flow service
     * @param ctx the project context
     */
    public FlowService(final ProjectContext ctx) {
        this.ctx = ctx;
    }

    /**
     * returns the project context
     * @return the project context
     */
    public ProjectContext getContext() {
        return ctx;
    }

    /**
     * validates if a timestamp is inside the testing phase window
     * @param currentTimestamp the current timestamp
     * @return an error message if outside testing phase null otherwise
     */
    public String validateTestingPhase(final String currentTimestamp) {
        LocalDate start = ctx.getStartDate();
        if (start == null) {
            return "Tickets can only be reported during testing phases.";
        }

        LocalDate now = LocalDate.parse(currentTimestamp);
        long days = ChronoUnit.DAYS.between(start, now) + 1;

        if (days < 1 || days > TESTING_PHASE_DAYS) {
            return "Tickets can only be reported during testing phases.";
        }

        return null;
    }
}
