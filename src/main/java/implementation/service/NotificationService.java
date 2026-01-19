package implementation.service;

import implementation.domain.milestone.Milestone;
import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.Ticket;
import implementation.domain.ticket.TicketStatus;
import implementation.domain.user.User;
import implementation.registry.MilestoneRegistry;
import implementation.registry.TicketRegistry;
import implementation.registry.UserRegistry;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * handles user notifications and dynamic notification generation
 * based on milestone due dates and blocked milestones
 */
public final class NotificationService {

    /**
     * observer used to deliver notification messages
     */
    public interface Observer {
        /**
         * receives a new notification message
         * @param message the notification message
         */
        void update(String message);
    }

    private final Map<String, Observer> observers = new HashMap<>();

    private final MilestoneRegistry milestoneRepo;
    private final TicketRegistry ticketRepo;
    private final UserRegistry userRepo;

    private final Set<String> dueNotified = new HashSet<>();
    private final Set<String> unblockNotified = new HashSet<>();

    /**
     * creates the notification service
     * @param milestoneRepo the milestone registry
     * @param ticketRepo the ticket registry
     * @param userRepo the user registry
     */
    public NotificationService(final MilestoneRegistry milestoneRepo,
                               final TicketRegistry ticketRepo,
                               final UserRegistry userRepo) {
        this.milestoneRepo = milestoneRepo;
        this.ticketRepo = ticketRepo;
        this.userRepo = userRepo;
    }

    /**
     * subscribes a user to notifications using a custom observer
     * @param username the username to subscribe
     * @param observer the observer implementation
     */
    public void subscribe(final String username, final Observer observer) {
        if (username == null || observer == null) {
            return;
        }
        observers.put(username, observer);
    }

    /**
     * notifies developers that a new milestone has been created
     * @param assignedDevs the assigned developer usernames
     * @param milestoneName the milestone name
     * @param dueDate the milestone due date
     */
    public void notifyNewMilestone(final List<String> assignedDevs,
                                   final String milestoneName,
                                   final LocalDate dueDate) {
        if (assignedDevs == null) {
            return;
        }

        String msg = "New milestone " + milestoneName
                + " has been created with due date " + dueDate + ".";

        for (int i = 0; i < assignedDevs.size(); i++) {
            notifyUser(assignedDevs.get(i), msg);
        }
    }

    /**
     * returns and clears notifications for a user
     * and also generates dynamic notifications before returning
     * @param username the username
     * @param timestamp the current timestamp in ISO format
     * @return the notifications list
     */
    public List<String> viewAndClear(final String username, final String timestamp) {
        LocalDate now = LocalDate.parse(timestamp);

        generateDynamic(username, now);

        User user = getUserOrNull(username);
        if (user == null) {
            return new ArrayList<>();
        }

        subscribeUserAsObserver(user);

        List<String> notes = copyList(user.getNotifications());
        user.clearNotifications();
        return notes;
    }

    /**
     * notifies a user with a message if an observer exists
     * if the user exists but has no observer, it auto-subscribes them
     * @param username the username
     * @param message the message
     */
    private void notifyUser(final String username, final String message) {
        if (username == null || message == null) {
            return;
        }

        Observer obs = observers.get(username);

        if (obs == null) {
            User user = getUserOrNull(username);
            if (user != null) {
                subscribeUserAsObserver(user);
                obs = observers.get(username);
            }
        }

        if (obs != null) {
            obs.update(message);
        }
    }

    /**
     * subscribes a user as an observer that calls user.update(...)
     * @param user the user
     */
    private void subscribeUserAsObserver(final User user) {
        if (user == null) {
            return;
        }

        Observer obs = new Observer() {
            @Override
            public void update(final String message) {
                user.update(message);
            }
        };

        subscribe(user.getUsername(), obs);
    }

    /**
     * finds a user by username
     * @param username the username to search for
     * @return the user if found null otherwise
     */
    private User getUserOrNull(final String username) {
        return userRepo.findByUsername(username);
    }

    /**
     * generates dynamic notifications for milestones assigned to the user
     * @param username the username
     * @param now the current date
     */
    private void generateDynamic(final String username, final LocalDate now) {
        List<Milestone> milestones = milestoneRepo.getAllForUser(username);

        for (int i = 0; i < milestones.size(); i++) {
            Milestone m = milestones.get(i);

            if (!isUserAssignedToMilestone(m, username)) {
                continue;
            }

            handleDueTomorrow(username, now, m);
            handleUnblockAfterDue(username, now, m);
        }
    }

    /**
     * checks if user is assigned to a milestone
     * @param m the milestone
     * @param username the username
     * @return true if assigned false otherwise
     */
    private boolean isUserAssignedToMilestone(final Milestone m, final String username) {
        if (m == null || username == null) {
            return false;
        }

        List<String> devs = m.getAssignedDevs();
        if (devs == null) {
            return false;
        }

        for (int i = 0; i < devs.size(); i++) {
            if (username.equals(devs.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * handles the "due tomorrow" dynamic notification
     * @param username the username
     * @param now the current date
     * @param m the milestone
     */
    private void handleDueTomorrow(final String username,
                                   final LocalDate now,
                                   final Milestone m) {
        long daysAfterDue = ChronoUnit.DAYS.between(m.getDueDate(), now);

        if (daysAfterDue != 1) {
            return;
        }

        String key = makeKey(username, m.getName(), "DUE");
        if (dueNotified.contains(key)) {
            return;
        }

        dueNotified.add(key);
        escalateUnresolvedTicketsToCritical(m);

        String msg = "Milestone " + m.getName()
                + " is due tomorrow. "
                + "All unresolved tickets are now CRITICAL.";

        notifyUser(username, msg);
    }

    /**
     * handles the "unblocked after due date" dynamic notification
     * @param username the username
     * @param now the current date
     * @param m the milestone
     */
    private void handleUnblockAfterDue(final String username,
                                       final LocalDate now,
                                       final Milestone m) {
        boolean blocked = milestoneRepo.isMilestoneBlocked(m.getName());

        if (!blocked) {
            return;
        }
        if (!now.isAfter(m.getDueDate())) {
            return;
        }

        String key = makeKey(username, m.getName(), "UNBLOCK");
        if (unblockNotified.contains(key)) {
            return;
        }

        unblockNotified.add(key);
        escalateUnresolvedTicketsToCritical(m);

        String msg = "Milestone " + m.getName()
                + " was unblocked after due date. "
                + "All active tickets are now CRITICAL.";

        notifyUser(username, msg);
    }

    /**
     * builds a unique notification key
     * @param username the username
     * @param milestoneName the milestone name
     * @param type the notification type
     * @return the key
     */
    private String makeKey(final String username,
                           final String milestoneName,
                           final String type) {
        return username + "|" + milestoneName + "|" + type;
    }

    /**
     * escalates unresolved tickets in a milestone to CRITICAL priority
     * @param m the milestone
     */
    private void escalateUnresolvedTicketsToCritical(final Milestone m) {
        List<Integer> ticketIds = m.getTickets();
        if (ticketIds == null) {
            return;
        }

        for (int i = 0; i < ticketIds.size(); i++) {
            Integer tid = ticketIds.get(i);
            if (tid == null) {
                continue;
            }

            Ticket t = ticketRepo.findById(tid);
            if (t == null) {
                continue;
            }

            if (t.getStatus() != TicketStatus.CLOSED) {
                t.setBusinessPriority(BusinessPriority.CRITICAL);
            }
        }
    }

    /**
     * copies a list of strings without using streams
     * @param original the original list
     * @return a new list containing the same elements
     */
    private List<String> copyList(final List<String> original) {
        List<String> out = new ArrayList<>();
        if (original == null) {
            return out;
        }

        for (int i = 0; i < original.size(); i++) {
            out.add(original.get(i));
        }
        return out;
    }
}
