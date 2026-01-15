package implementation.service;

import implementation.domain.milestone.Milestone;
import implementation.domain.ticket.Ticket;
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
            map.put("status", "ACTIVE");

            map.put("isBlocked", milestoneRepo.isMilestoneBlocked(m.getName()));

            long diff = ChronoUnit.DAYS.between(now, m.getDueDate());

            if (diff > 0) {
                map.put("daysUntilDue", diff + 1);
                map.put("overdueBy", 0);
            } else if (diff == 0) {
                map.put("daysUntilDue", 1);
                map.put("overdueBy", 0);
            } else {
                map.put("daysUntilDue", 0);
                map.put("overdueBy", Math.abs(diff) + 1);
            }

            List<Integer> openTickets = new ArrayList<>();
            List<Integer> closedTickets = new ArrayList<>();
            for (Integer tId : m.getTickets()) {
                ticketRepo.findById(tId).ifPresentOrElse(t -> {
                    if (t.getStatus() == TicketStatus.CLOSED) closedTickets.add(tId);
                    else openTickets.add(tId);
                }, () -> openTickets.add(tId));
            }
            map.put("openTickets", openTickets);
            map.put("closedTickets", closedTickets);

            double percentage = m.getTickets().isEmpty() ? 0.0 :
                    (double) closedTickets.size() / m.getTickets().size() * 100;
            map.put("completionPercentage", percentage);

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
            map.put("repartition", repartition);
            output.add(map);
        }
        return output;
    }
}
