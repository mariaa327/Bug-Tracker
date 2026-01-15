package implementation.service;

import implementation.domain.milestone.Milestone;
import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.TicketStatus;
import implementation.repository.impl.InMemoryMilestoneRepository;
import implementation.repository.impl.InMemoryTicketRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class NotificationService {

    private final InMemoryMilestoneRepository milestoneRepo;
    private final InMemoryTicketRepository ticketRepo;

    private final Map<String, List<String>> inbox = new HashMap<>();
    private final Set<String> dueNotified = new HashSet<>();
    private final Set<String> unblockNotified = new HashSet<>();

    public NotificationService(InMemoryMilestoneRepository milestoneRepo,
                               InMemoryTicketRepository ticketRepo) {
        this.milestoneRepo = milestoneRepo;
        this.ticketRepo = ticketRepo;
    }

    public void notifyNewMilestone(List<String> assignedDevs, String milestoneName, LocalDate dueDate) {
        String msg = "New milestone " + milestoneName + " has been created with due date " + dueDate + ".";
        for (String dev : assignedDevs) {
            inbox.computeIfAbsent(dev, k -> new ArrayList<>()).add(msg);
        }
    }

    public List<String> viewAndClear(String username, String timestamp) {
        LocalDate now = LocalDate.parse(timestamp);

        generateDynamic(username, now);

        List<String> notes = inbox.getOrDefault(username, new ArrayList<>());
        inbox.put(username, new ArrayList<>());
        return notes;
    }

    private void generateDynamic(String username, LocalDate now) {
        List<Milestone> milestones = milestoneRepo.getAllForUser(username);

        for (Milestone m : milestones) {
            if (!m.getAssignedDevs().contains(username)) continue;

            long daysAfterDue = ChronoUnit.DAYS.between(m.getDueDate(), now);
            if (daysAfterDue == 1) {
                String key = username + "|" + m.getName() + "|DUE";
                if (!dueNotified.contains(key)) {
                    dueNotified.add(key);

                    for (Integer tid : m.getTickets()) {
                        ticketRepo.findById(tid).ifPresent(t -> {
                            if (t.getStatus() != TicketStatus.CLOSED) {
                                t.setBusinessPriority(BusinessPriority.CRITICAL);
                            }
                        });
                    }

                    inbox.computeIfAbsent(username, k -> new ArrayList<>()).add(
                            "Milestone " + m.getName() + " is due tomorrow. All unresolved tickets are now CRITICAL."
                    );
                }
            }

            boolean blocked = milestoneRepo.isMilestoneBlocked(m.getName());
            if (blocked && now.isAfter(m.getDueDate())) {
                String key = username + "|" + m.getName() + "|UNBLOCK";
                if (!unblockNotified.contains(key)) {
                    unblockNotified.add(key);

                    for (Integer tid : m.getTickets()) {
                        ticketRepo.findById(tid).ifPresent(t -> {
                            if (t.getStatus() != TicketStatus.CLOSED) {
                                t.setBusinessPriority(BusinessPriority.CRITICAL);
                            }
                        });
                    }

                    inbox.computeIfAbsent(username, k -> new ArrayList<>()).add(
                            "Milestone " + m.getName() + " was unblocked after due date. All active tickets are now CRITICAL."
                    );
                }
            }
        }
    }
}
