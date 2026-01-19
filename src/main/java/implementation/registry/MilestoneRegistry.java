package implementation.registry;

import implementation.domain.milestone.Milestone;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * in-memory registry for Milestone objects
 * this registry stores milestones in a simple list and provides helper
 * queries used by services and commands
 */
public final class MilestoneRegistry {
    private final List<Milestone> milestones = new ArrayList<>();

    /**
     * adds a milestone to the registry
     * @param milestone the milestone to add; if null, nothing is added
     */
    public void add(final Milestone milestone) {
        if (milestone == null) {
            return;
        }
        milestones.add(milestone);
    }

    /**
     * returns all milestones relevant to a user (created by the user or
     * assigned to them)
     * the result is sorted by due date, then by milestone name
     * @param username the username used for filtering
     * @return a list of milestones relevant to the user, empty list if none
     * or invalid input
     */
    public List<Milestone> getAllForUser(final String username) {
        List<Milestone> result = new ArrayList<>();
        if (username == null || username.isEmpty()) {
            return result;
        }

        for (Milestone m : milestones) {
            if (belongsToUser(m, username)) {
                result.add(m);
            }
        }

        result.sort(new Comparator<Milestone>() {
            @Override
            public int compare(final Milestone a, final Milestone b) {
                int c = a.getDueDate().compareTo(b.getDueDate());
                if (c != 0) {
                    return c;
                }
                return a.getName().compareTo(b.getName());
            }
        });

        return result;
    }

    /**
     * checks if a milestone belongs to a user (either created by the user or
     * assigned to them)
     * @param m the milestone to check
     * @param username the username to match
     * @return true if the milestone belongs to the user, false
     * otherwise
     */
    private boolean belongsToUser(final Milestone m, final String username) {
        if (m == null) {
            return false;
        }

        if (username.equals(m.getCreatedBy())) {
            return true;
        }

        List<String> assigned = m.getAssignedDevs();
        if (assigned == null) {
            return false;
        }

        for (String dev : assigned) {
            if (username.equals(dev)) {
                return true;
            }
        }

        return false;
    }

    /**
     * checks if a milestone is blocked by another milestone
     * a milestone is considered blocked if there exists at least one
     * milestone whose blockingfor list contains the provided milestone name
     * @param milestoneName the milestone name to check
     * @return true if the milestone is blocked, false otherwise
     */
    public boolean isMilestoneBlocked(final String milestoneName) {
        if (milestoneName == null || milestoneName.isEmpty()) {
            return false;
        }

        for (Milestone m : milestones) {
            if (m == null) {
                continue;
            }

            List<String> blockingFor = m.getBlockingFor();
            if (blockingFor == null) {
                continue;
            }

            for (String name : blockingFor) {
                if (milestoneName.equals(name)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * finds the name of the milestone that contains a given ticket id
     * @param ticketId the ticket id to search for
     * @return the milestone name if found, null otherwise
     */
    public String getMilestoneNameByTicket(final Integer ticketId) {
        if (ticketId == null) {
            return null;
        }

        Milestone m = findByTicketId(ticketId);
        if (m == null) {
            return null;
        }

        return m.getName();
    }

    /**
     * finds the milestone that contains a given ticket id
     * @param ticketId the ticket id to search for
     * @return the matching milestone, null if no milestone contains the ticket
     */
    public Milestone findByTicketId(final int ticketId) {
        for (Milestone m : milestones) {
            if (m == null) {
                continue;
            }

            List<Integer> tickets = m.getTickets();
            if (tickets == null) {
                continue;
            }

            for (Integer id : tickets) {
                if (id != null && id.intValue() == ticketId) {
                    return m;
                }
            }
        }

        return null;
    }

    /**
     * checks if a developer is assigned to the milestone that contains a
     * given ticket
     * @param ticketId the ticket id
     * @param devUsername the developer username to check
     * @return true if the developer is assigned to the ticket's
     * milestone, false otherwise
     */
    public boolean isDevAssignedToTicketMilestone(final int ticketId, final String devUsername) {
        if (devUsername == null || devUsername.isEmpty()) {
            return false;
        }

        Milestone m = findByTicketId(ticketId);
        if (m == null) {
            return false;
        }

        List<String> assigned = m.getAssignedDevs();
        if (assigned == null) {
            return false;
        }

        for (String dev : assigned) {
            if (devUsername.equals(dev)) {
                return true;
            }
        }

        return false;
    }

    /**
     * returns a copy of all milestones stored in the registry
     * @return a new list containing all milestones
     */
    public List<Milestone> getAll() {
        return new ArrayList<>(milestones);
    }
}
