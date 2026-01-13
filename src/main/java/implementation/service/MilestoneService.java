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

            List<Integer> openTickets = new ArrayList<>();
            List<Integer> closedTickets = new ArrayList<>();
            splitTicketsByStatus(m, openTickets, closedTickets);

            map.put("openTickets", openTickets);
            map.put("closedTickets", closedTickets);

            int total = m.getTickets().size();
            int closed = closedTickets.size();

            boolean completed = (total > 0 && closed == total);
            map.put("status", completed ? "COMPLETED" : "ACTIVE");

            LocalDate referenceDate = completed ? completionDateOrNow(m, now) : now;

            long diff = ChronoUnit.DAYS.between(referenceDate, m.getDueDate());
            putDueFieldsInclusive(map, diff);

            map.put("completionPercentage", completionRatio(closed, total));

            map.put("repartition", buildRepartition(m));

            output.add(map);
        }

        return output;
    }

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
