package implementation.service.helpers;

import implementation.domain.ticket.Ticket;
import implementation.domain.ticket.TicketStatus;
import implementation.registry.MilestoneRegistry;
import implementation.registry.TicketRegistry;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * helper service that handles ticket queries and views for different roles,
 * including sorting and milestone escalation updates before returning results
 */
public final class TicketSearchService {
    private final TicketRegistry repo;
    private final MilestoneRegistry milestoneRepo;
    private final MilestonePriorityPolicy escalationPolicy;

    /**
     * creates the ticket query service
     * @param repo the ticket registry
     * @param milestoneRepo the milestone registry
     * @param escalationPolicy the escalation policy
     */
    public TicketSearchService(final TicketRegistry repo,
                               final MilestoneRegistry milestoneRepo,
                               final MilestonePriorityPolicy escalationPolicy) {
        this.repo = repo;
        this.milestoneRepo = milestoneRepo;
        this.escalationPolicy = escalationPolicy;
    }

    /**
     * views all tickets as manager
     * @param currentTimestamp the current timestamp
     * @return the sorted list of tickets
     */
    public List<Ticket> viewTicketsAsManager(final String currentTimestamp) {
        LocalDate now = LocalDate.parse(currentTimestamp);

        List<Ticket> tickets = copyTickets(repo.getAll());
        escalationPolicy.applyEscalation(tickets, now);
        sortByCreatedAtThenId(tickets);

        return tickets;
    }

    /**
     * views tickets as reporter
     * @param username the reporter username
     * @param currentTimestamp the current timestamp
     * @return tickets reported by that user
     */
    public List<Ticket> viewTicketsAsReporter(final String username,
                                              final String currentTimestamp) {
        LocalDate now = LocalDate.parse(currentTimestamp);

        List<Ticket> tickets = new ArrayList<>();
        List<Ticket> all = repo.getAll();

        for (int i = 0; i < all.size(); i++) {
            Ticket t = all.get(i);
            if (username != null && username.equals(t.getReportedBy())) {
                tickets.add(t);
            }
        }

        escalationPolicy.applyEscalation(tickets, now);
        sortByCreatedAtThenId(tickets);

        return tickets;
    }

    /**
     * views tickets as developer
     * @param devUsername the developer username
     * @param currentTimestamp the current timestamp
     * @return tickets available for assignment
     */
    public List<Ticket> viewTicketsAsDeveloper(final String devUsername,
                                               final String currentTimestamp) {
        LocalDate now = LocalDate.parse(currentTimestamp);

        List<Ticket> tickets = new ArrayList<>();
        List<Ticket> all = repo.getAll();

        for (int i = 0; i < all.size(); i++) {
            Ticket t = all.get(i);

            boolean isOpen = (t.getStatus() == TicketStatus.OPEN);
            boolean unassigned = (t.getAssignedTo() == null || t.getAssignedTo().isEmpty());
            boolean inMyMilestone =
                    milestoneRepo.isDevAssignedToTicketMilestone(t.getId(), devUsername);

            if (isOpen && unassigned && inMyMilestone) {
                tickets.add(t);
            }
        }

        escalationPolicy.applyEscalation(tickets, now);
        sortByCreatedAtThenId(tickets);

        return tickets;
    }

    /**
     * views tickets assigned to a developer
     * @param devUsername the developer username
     * @param timestamp the current timestamp
     * @return assigned tickets sorted by priority desc then id asc
     */
    public List<Ticket> viewAssignedTickets(final String devUsername, final String timestamp) {
        List<Ticket> assigned = new ArrayList<>();
        List<Ticket> all = repo.getAll();

        for (int i = 0; i < all.size(); i++) {
            Ticket t = all.get(i);
            if (devUsername != null && devUsername.equals(t.getAssignedTo())) {
                assigned.add(t);
            }
        }

        assigned.sort(new Comparator<Ticket>() {
            @Override
            public int compare(final Ticket a, final Ticket b) {
                int pa = a.getBusinessPriority().score();
                int pb = b.getBusinessPriority().score();
                if (pa != pb) {
                    return Integer.compare(pb, pa);
                }
                return Integer.compare(a.getId(), b.getId());
            }
        });

        return assigned;
    }

    /**
     * views ticket history for a developer
     * @param devUsername the developer username
     * @return tickets ever assigned to the developer sorted by id
     */
    public List<Ticket> viewTicketHistoryTickets(final String devUsername) {
        List<Ticket> out = new ArrayList<>();
        List<Ticket> all = repo.getAll();

        for (int i = 0; i < all.size(); i++) {
            Ticket t = all.get(i);
            if (wasEverAssignedTo(t, devUsername)) {
                out.add(t);
            }
        }

        out.sort(Comparator.comparingInt(Ticket::getId));
        return out;
    }

    /**
     * checks if a ticket was ever assigned to a developer
     * @param t the ticket
     * @param devUsername the developer username
     * @return true if ever assigned false otherwise
     */
    private boolean wasEverAssignedTo(final Ticket t, final String devUsername) {
        List<Map<String, Object>> actions = t.getActions();
        for (int i = 0; i < actions.size(); i++) {
            Map<String, Object> a = actions.get(i);
            Object action = a.get("action");
            Object by = a.get("by");

            if ("ASSIGNED".equals(action) && devUsername != null && devUsername.equals(by)) {
                return true;
            }
        }
        return false;
    }

    /**
     * copies tickets into a new list
     * @param original the original list
     * @return a copy list
     */
    private List<Ticket> copyTickets(final List<Ticket> original) {
        return new ArrayList<>(original);
    }

    /**
     * sorts tickets by createdAt then id
     * @param tickets the tickets to sort
     */
    private void sortByCreatedAtThenId(final List<Ticket> tickets) {
        Collections.sort(tickets, new Comparator<Ticket>() {
            @Override
            public int compare(final Ticket a, final Ticket b) {
                int c = a.getCreatedAt().compareTo(b.getCreatedAt());
                if (c != 0) {
                    return c;
                }
                return Integer.compare(a.getId(), b.getId());
            }
        });
    }
}
