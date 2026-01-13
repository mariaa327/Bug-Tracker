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
            String existing = milestoneRepo.getMilestoneNameByTicket(ticketId);
            if (existing != null) {
                return "Tickets " + ticketId + " already assigned to milestone " + existing + ".";
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
            for (Integer tid : m.getTickets()) {
                ticketRepo.findById(tid).ifPresentOrElse(t -> {
                    if (t.getStatus() == TicketStatus.CLOSED) closedTickets.add(tid);
                    else openTickets.add(tid);
                }, () -> openTickets.add(tid));
            }

            map.put("openTickets", openTickets);
            map.put("closedTickets", closedTickets);

            int total = m.getTickets().size();
            int closed = closedTickets.size();
            map.put("status", (total > 0 && closed == total) ? "COMPLETED" : "ACTIVE");

            long diff = ChronoUnit.DAYS.between(now, m.getDueDate());
            map.put("daysUntilDue", diff >= 0 ? diff + 1 : 0);
            map.put("overdueBy", diff < 0 ? Math.abs(diff) + 1 : 0);

            double completion = total == 0 ? 0.0 : Math.round(((double) closed / total) * 100.0) / 100.0;
            map.put("completionPercentage", completion);

            List<Map<String, Object>> repartition = new ArrayList<>();
            for (String dev : m.getAssignedDevs()) {
                Map<String, Object> devMap = new LinkedHashMap<>();
                devMap.put("developer", dev);

                List<Integer> assignedTickets = new ArrayList<>();
                for (Integer tid : m.getTickets()) {
                    ticketRepo.findById(tid).ifPresent(t -> {
                        if (dev.equals(t.getAssignedTo())) assignedTickets.add(tid);
                    });
                }

                devMap.put("assignedTickets", assignedTickets);
                repartition.add(devMap);
            }
            map.put("repartition", repartition);

            output.add(map);
        }

        return output;
    }
}
