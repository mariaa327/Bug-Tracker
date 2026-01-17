package implementation.service;

import implementation.domain.milestone.Milestone;
import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.TicketStatus;
import implementation.domain.user.User;
import implementation.repository.impl.InMemoryMilestoneRepository;
import implementation.repository.impl.InMemoryTicketRepository;
import implementation.repository.impl.JsonUserRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class NotificationService {

    public interface Observer {
        void update(String message);
    }

    private final Map<String, Observer> observers = new HashMap<>();

    public void subscribe(String username, Observer observer) {
        if (username == null || observer == null) return;
        observers.put(username, observer);
    }

    public void unsubscribe(String username) {
        observers.remove(username);
    }

    private void notifyUser(String username, String message) {
        Observer obs = observers.get(username);

        if (obs == null) {
            Optional<User> uOpt = userRepo.findByUsername(username);
            if (uOpt.isPresent()) {
                User u = uOpt.get();
                subscribe(u.getUsername(), u::update);
                obs = observers.get(username);
            }
        }

        if (obs != null) {
            obs.update(message);
        }
    }

    private final InMemoryMilestoneRepository milestoneRepo;
    private final InMemoryTicketRepository ticketRepo;
    private final JsonUserRepository userRepo;

    private final Set<String> dueNotified = new HashSet<>();
    private final Set<String> unblockNotified = new HashSet<>();

    public NotificationService(InMemoryMilestoneRepository milestoneRepo,
                               InMemoryTicketRepository ticketRepo,
                               JsonUserRepository userRepo) {
        this.milestoneRepo = milestoneRepo;
        this.ticketRepo = ticketRepo;
        this.userRepo = userRepo;
    }

    public void notifyNewMilestone(List<String> assignedDevs, String milestoneName, LocalDate dueDate) {
        String msg = "New milestone " + milestoneName + " has been created with due date " + dueDate + ".";
        for (String dev : assignedDevs) {
            notifyUser(dev, msg);
        }
    }

    public List<String> viewAndClear(String username, String timestamp) {
        LocalDate now = LocalDate.parse(timestamp);

        generateDynamic(username, now);

        Optional<User> uOpt = userRepo.findByUsername(username);
        if (uOpt.isEmpty()) {
            return new ArrayList<>();
        }

        User u = uOpt.get();
        subscribe(u.getUsername(), u::update);

        List<String> notes = new ArrayList<>(u.getNotifications());
        u.clearNotifications();
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

                    notifyUser(username,
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

                    notifyUser(username,
                            "Milestone " + m.getName() +
                                    " was unblocked after due date. All active tickets are now CRITICAL."
                    );
                }
            }
        }
    }
}
