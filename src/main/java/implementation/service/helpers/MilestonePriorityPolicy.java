package implementation.service.helpers;

import implementation.domain.flow.ProjectContext;
import implementation.domain.milestone.Milestone;
import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.Ticket;
import implementation.domain.ticket.TicketStatus;
import implementation.registry.MilestoneRegistry;
import implementation.service.FlowService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * helper used to apply milestone-based priority escalation rules on open tickets
 * depending on due dates and periodic bump intervals
 */
public final class MilestonePriorityPolicy {
    private static final int BUMP_PERIOD_DAYS = 3;

    private final FlowService flowService;
    private final MilestoneRegistry milestoneRepo;

    private final Map<Integer, LocalDate> lastEscalatedOn = new HashMap<>();

    /**
     * creates the milestone escalation policy
     * @param flowService the flow service
     * @param milestoneRepo the milestone registry
     */
    public MilestonePriorityPolicy(final FlowService flowService,
                                   final MilestoneRegistry milestoneRepo) {
        this.flowService = flowService;
        this.milestoneRepo = milestoneRepo;
    }

    /**
     * applies milestone escalation rules on a list of tickets
     * @param tickets the tickets
     * @param now the current date
     */
    public void applyEscalation(final List<Ticket> tickets, final LocalDate now) {
        ProjectContext ctx = flowService.getContext();
        if (ctx.getStartDate() == null) {
            return;
        }

        for (int i = 0; i < tickets.size(); i++) {
            Ticket t = tickets.get(i);

            if (t.getStatus() != TicketStatus.OPEN) {
                continue;
            }

            Milestone m = milestoneRepo.findByTicketId(t.getId());
            if (m == null) {
                continue;
            }

            if (milestoneRepo.isMilestoneBlocked(m.getName())) {
                continue;
            }

            applyDueTomorrowRule(t, m, now);
            applyPeriodicBumpsRule(t, m, now);
        }
    }

    /**
     * applies "due tomorrow => CRITICAL" rule
     * @param t the ticket
     * @param m the milestone
     * @param now the current date
     */
    private void applyDueTomorrowRule(final Ticket t, final Milestone m, final LocalDate now) {
        if (!now.equals(m.getDueDate().minusDays(1))) {
            return;
        }

        if (t.getBusinessPriority() != BusinessPriority.CRITICAL) {
            t.setBusinessPriority(BusinessPriority.CRITICAL);
        }

        lastEscalatedOn.put(t.getId(), now);
    }

    /**
     * applies periodic bumps every 3 days from milestone creation
     * @param t the ticket
     * @param m the milestone
     * @param now the current date
     */
    private void applyPeriodicBumpsRule(final Ticket t, final Milestone m, final LocalDate now) {
        if (now.equals(m.getDueDate().minusDays(1))) {
            return;
        }

        LocalDate last = lastEscalatedOn.get(t.getId());
        if (last == null) {
            last = m.getCreatedAt();
        }

        int bumps = numberOfBumpsBetween(m.getCreatedAt(), last, now);
        for (int b = 0; b < bumps; b++) {
            t.setBusinessPriority(t.getBusinessPriority().bump());
        }

        lastEscalatedOn.put(t.getId(), now);
    }

    /**
     * counts how many bump days exist between (last, now] relative to createdAt
     * @param createdAt the milestone created at date
     * @param last the last processed date
     * @param now the current date
     * @return the number of bumps
     */
    private int numberOfBumpsBetween(final LocalDate createdAt,
                                     final LocalDate last,
                                     final LocalDate now) {
        if (now.isBefore(last)) {
            return 0;
        }

        long lastDayIndex = ChronoUnit.DAYS.between(createdAt, last);
        long nowDayIndex = ChronoUnit.DAYS.between(createdAt, now);

        long start = lastDayIndex + 1;
        long end = nowDayIndex;

        int bumps = 0;
        for (long d = start; d <= end; d++) {
            if (d > 0 && d % BUMP_PERIOD_DAYS == 0) {
                bumps++;
            }
        }

        return bumps;
    }
}
