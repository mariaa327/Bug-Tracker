package implementation.service;

import implementation.domain.milestone.Milestone;
import implementation.domain.ticket.TicketStatus;
import implementation.dto.input.CommandInput;
import implementation.repository.impl.InMemoryMilestoneRepository;
import implementation.repository.impl.InMemoryTicketRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class MilestoneService {
    private final InMemoryMilestoneRepository milestoneRepo;
    private final InMemoryTicketRepository ticketRepo;

    public MilestoneService(InMemoryMilestoneRepository milestoneRepo, InMemoryTicketRepository ticketRepo) {
        this.milestoneRepo = milestoneRepo;
        this.ticketRepo = ticketRepo;
    }

    public String createMilestone(CommandInput input) {
        for (Integer ticketId : input.getTickets()) {
            String existingMilestone = milestoneRepo.getMilestoneNameByTicket(ticketId);
            if (existingMilestone != null) {
                return "Tickets " + ticketId + " already assigned to milestone " + existingMilestone + ".";
            }
        }

        Milestone m = new Milestone(
                input.getName(),
                input.getBlockingFor(),
                LocalDate.parse(input.getDueDate()),
                LocalDate.parse(input.getTimestamp()),
                input.getTickets(),
                input.getAssignedDevs(),
                input.getUsername()
        );

        milestoneRepo.add(m);

        // pentru viewTicketHistory (Test 8): ADDED_TO_MILESTONE
        for (Integer ticketId : input.getTickets()) {
            ticketRepo.findById(ticketId).ifPresent(t ->
                    t.logAddedToMilestone(input.getName(), input.getUsername(), input.getTimestamp())
            );
        }

        return null;
    }

    public List<Map<String, Object>> viewMilestones(String username, String timestamp) {
        LocalDate now = LocalDate.parse(timestamp);
        List<Milestone> milestones = milestoneRepo.getAllForUser(username);

        List<Map<String, Object>> output = new ArrayList<>();
        for (Milestone m : milestones) {
            Map<String, Object> map = new LinkedHashMap<>();

            map.put("name", m.getName());
            map.put("blockingFor", m.getBlockingFor());
            map.put("dueDate", m.getDueDate().toString());
            map.put("createdAt", m.getCreatedAt().toString());
            map.put("tickets", m.getTickets());
            map.put("assignedDevs", m.getAssignedDevs());
            map.put("createdBy", m.getCreatedBy());

            boolean blocked = milestoneRepo.isMilestoneBlocked(m.getName());
            map.put("isBlocked", blocked);

            // Tickets split
            List<Integer> openTickets = new ArrayList<>();
            List<Integer> closedTickets = new ArrayList<>();
            splitTicketsByStatus(m, openTickets, closedTickets);

            map.put("openTickets", openTickets);
            map.put("closedTickets", closedTickets);

            int total = m.getTickets().size();
            int closed = closedTickets.size();

            boolean completed = (total > 0 && closed == total);
            map.put("status", completed ? "COMPLETED" : "ACTIVE");

            // Alegem data de referinta pentru overdue:
            // - milestone ACTIVE -> now (timestamp-ul comenzii)
            // - milestone COMPLETED -> ziua cand s-a inchis ultimul ticket (max closedAt)
            LocalDate referenceDate = completed ? completionDateOrNow(m, now) : now;

            long diff = ChronoUnit.DAYS.between(referenceDate, m.getDueDate());
            putDueFieldsInclusive(map, diff);

            // completionPercentage ca fractie 0..1, rotunjit la 2 zecimale
            map.put("completionPercentage", completionRatio(closed, total));

            map.put("repartition", buildRepartition(m));

            output.add(map);
        }

        return output;
    }

    /**
     * Pentru milestone COMPLETED: data la care s-a inchis ultimul ticket (max(closedAt)).
     * Daca, din orice motiv, nu avem closedAt pe vreun ticket, cadem pe now ca fallback safe.
     */
    private LocalDate completionDateOrNow(Milestone m, LocalDate now) {
        LocalDate completionDate = null;

        for (Integer tId : m.getTickets()) {
            var tOpt = ticketRepo.findById(tId);
            if (tOpt.isPresent() && tOpt.get().getClosedAt() != null) {
                LocalDate c = tOpt.get().getClosedAt();
                if (completionDate == null || c.isAfter(completionDate)) {
                    completionDate = c;
                }
            }
        }

        return (completionDate != null) ? completionDate : now;
    }

    /**
     * Regula "inclusiva" (cum a cerut checker-ul la Test 8):
     * - daca nu e overdue: daysUntilDue = diff + 1, overdueBy = 0
     * - daca e overdue: daysUntilDue = 0, overdueBy = abs(diff) + 1
     */
    private static void putDueFieldsInclusive(Map<String, Object> map, long diffDays) {
        long daysUntilDue = diffDays >= 0 ? diffDays + 1 : 0;
        long overdueBy = diffDays < 0 ? Math.abs(diffDays) + 1 : 0;

        map.put("daysUntilDue", daysUntilDue);
        map.put("overdueBy", overdueBy);
    }

    private void splitTicketsByStatus(Milestone m, List<Integer> openTickets, List<Integer> closedTickets) {
        for (Integer tId : m.getTickets()) {
            ticketRepo.findById(tId).ifPresentOrElse(t -> {
                if (t.getStatus() == TicketStatus.CLOSED) {
                    closedTickets.add(tId);
                } else {
                    openTickets.add(tId);
                }
            }, () -> openTickets.add(tId));
        }
    }

    private List<Map<String, Object>> buildRepartition(Milestone m) {
        List<Map<String, Object>> repartition = new ArrayList<>();

        for (String dev : m.getAssignedDevs()) {
            Map<String, Object> devMap = new LinkedHashMap<>();
            devMap.put("developer", dev);

            List<Integer> assignedTickets = new ArrayList<>();
            for (Integer tId : m.getTickets()) {
                var tOpt = ticketRepo.findById(tId);
                if (tOpt.isPresent() && dev.equals(tOpt.get().getAssignedTo())) {
                    assignedTickets.add(tId);
                }
            }

            devMap.put("assignedTickets", assignedTickets);
            repartition.add(devMap);
        }

        return repartition;
    }

    private static double completionRatio(int closed, int total) {
        if (total == 0) return 0.0;
        return round2((double) closed / (double) total);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
