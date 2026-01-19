package implementation.service;

import implementation.domain.milestone.Milestone;
import implementation.domain.ticket.Ticket;
import implementation.domain.ticket.TicketStatus;
import implementation.input.CommandInput;
import implementation.registry.MilestoneRegistry;
import implementation.registry.TicketRegistry;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * handles milestone creation and milestone reports for users
 */
public final class MilestoneService {

    private static final double HUNDRED = 100.0;

    private final MilestoneRegistry milestoneRepo;
    private final TicketRegistry ticketRepo;

    /**
     * creates a milestone service
     * @param milestoneRepo the milestone registry
     * @param ticketRepo the ticket registry
     */
    public MilestoneService(final MilestoneRegistry milestoneRepo,
                            final TicketRegistry ticketRepo) {
        this.milestoneRepo = milestoneRepo;
        this.ticketRepo = ticketRepo;
    }

    /**
     * creates a milestone from a command input
     * @param input the command input
     * @return an error message if validation fails null otherwise
     */
    public String createMilestone(final CommandInput input) {
        List<Integer> ticketIds = input.getTickets();

        if (ticketIds != null) {
            for (Integer ticketId : ticketIds) {
                String existing = milestoneRepo.getMilestoneNameByTicket(ticketId);
                if (existing != null) {
                    return "Tickets " + ticketId
                            + " already assigned to milestone "
                            + existing + ".";
                }
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

        if (ticketIds != null) {
            for (Integer ticketId : ticketIds) {
                Ticket t = getTicketOrNull(ticketId);
                if (t != null) {
                    t.logAddedToMilestone(
                            input.getName(),
                            input.getUsername(),
                            input.getTimestamp()
                    );
                }
            }
        }

        return null;
    }

    /**
     * builds the milestone view for a user
     * @param username the username
     * @param timestamp the current timestamp
     * @return a list of milestone maps
     */
    public List<Map<String, Object>> viewMilestones(final String username,
                                                    final String timestamp) {
        LocalDate now = LocalDate.parse(timestamp);

        List<Milestone> milestones = milestoneRepo.getAllForUser(username);
        List<Map<String, Object>> output = new ArrayList<>();

        for (Milestone m : milestones) {
            Map<String, Object> map = new LinkedHashMap<>();

            fillMilestoneBasicFields(map, m);

            boolean blocked = milestoneRepo.isMilestoneBlocked(m.getName());
            map.put("isBlocked", blocked);

            List<Integer> openTickets = new ArrayList<>();
            List<Integer> closedTickets = new ArrayList<>();
            splitTickets(m, openTickets, closedTickets);

            map.put("openTickets", openTickets);
            map.put("closedTickets", closedTickets);

            int total = safeSize(m.getTickets());
            int closed = closedTickets.size();
            boolean completed = (total > 0 && closed == total);

            map.put("status", completed ? "COMPLETED" : "ACTIVE");

            LocalDate referenceDate;
            if (completed) {
                referenceDate = getCompletionDate(m, now);
            } else {
                referenceDate = now;
            }

            putDueInfo(map, referenceDate, m.getDueDate());

            map.put("completionPercentage", completionRatio(closed, total));
            map.put("repartition", buildRepartition(m));

            output.add(map);
        }

        return output;
    }

    /**
     * fills basic milestone fields in the output map
     * @param map the output map
     * @param m the milestone
     */
    private void fillMilestoneBasicFields(final Map<String, Object> map,
                                          final Milestone m) {
        map.put("name", m.getName());
        map.put("blockingFor", m.getBlockingFor());
        map.put("dueDate", m.getDueDate().toString());
        map.put("createdAt", m.getCreatedAt().toString());
        map.put("tickets", m.getTickets());
        map.put("assignedDevs", m.getAssignedDevs());
        map.put("createdBy", m.getCreatedBy());
    }

    /**
     * splits milestone tickets into open and closed lists
     * @param m the milestone
     * @param openTickets the open tickets list
     * @param closedTickets the closed tickets list
     */
    private void splitTickets(final Milestone m,
                              final List<Integer> openTickets,
                              final List<Integer> closedTickets) {
        List<Integer> ids = m.getTickets();
        if (ids == null) {
            return;
        }

        for (Integer tId : ids) {
            Ticket t = getTicketOrNull(tId);

            if (t == null) {
                openTickets.add(tId);
                continue;
            }

            if (t.getStatus() == TicketStatus.CLOSED) {
                closedTickets.add(tId);
            } else {
                openTickets.add(tId);
            }
        }
    }

    /**
     * computes the milestone completion date as the latest ticket closed date
     * @param m the milestone
     * @param fallback the fallback date
     * @return the completion date if available fallback otherwise
     */
    private LocalDate getCompletionDate(final Milestone m,
                                        final LocalDate fallback) {
        List<Integer> ids = m.getTickets();
        if (ids == null) {
            return fallback;
        }

        LocalDate latestClosedAt = null;

        for (Integer tId : ids) {
            Ticket t = getTicketOrNull(tId);
            if (t == null) {
                continue;
            }

            LocalDate closedAt = t.getClosedAt();
            if (closedAt == null) {
                continue;
            }

            if (latestClosedAt == null || closedAt.isAfter(latestClosedAt)) {
                latestClosedAt = closedAt;
            }
        }

        if (latestClosedAt == null) {
            return fallback;
        }
        return latestClosedAt;
    }

    /**
     * adds due fields to milestone map
     * @param map the output map
     * @param referenceDate the reference date for calculations
     * @param dueDate the due date
     */
    private void putDueInfo(final Map<String, Object> map,
                            final LocalDate referenceDate,
                            final LocalDate dueDate) {
        long diff = ChronoUnit.DAYS.between(referenceDate, dueDate);

        long daysUntilDue;
        long overdueBy;

        if (diff >= 0) {
            daysUntilDue = diff + 1;
            overdueBy = 0;
        } else {
            daysUntilDue = 0;
            overdueBy = (-diff) + 1;
        }

        map.put("daysUntilDue", daysUntilDue);
        map.put("overdueBy", overdueBy);
    }

    /**
     * builds repartition list for a milestone
     * @param m the milestone
     * @return list of repartition maps
     */
    private List<Map<String, Object>> buildRepartition(final Milestone m) {
        List<String> devs = safeListStrings(m.getAssignedDevs());
        List<Integer> tickets = safeListInts(m.getTickets());

        List<Map<String, Object>> repartition = new ArrayList<>();

        for (String dev : devs) {
            Map<String, Object> devMap = new LinkedHashMap<>();
            devMap.put("developer", dev);
            devMap.put("assignedTickets", findTicketsAssignedTo(dev, tickets));
            repartition.add(devMap);
        }

        return repartition;
    }

    /**
     * finds ticket ids assigned to a developer
     * @param dev the developer username
     * @param ticketIds the ticket ids to check
     * @return list of ticket ids assigned to the developer
     */
    private List<Integer> findTicketsAssignedTo(final String dev,
                                                final List<Integer> ticketIds) {
        List<Integer> assigned = new ArrayList<>();

        for (Integer tId : ticketIds) {
            Ticket t = getTicketOrNull(tId);
            if (t == null) {
                continue;
            }

            String assignedTo = t.getAssignedTo();
            if (dev.equals(assignedTo)) {
                assigned.add(tId);
            }
        }

        return assigned;
    }

    /**
     * finds a ticket by id
     * @param ticketId the ticket id to search for
     * @return the ticket if found null otherwise
     */
    private Ticket getTicketOrNull(final int ticketId) {
        return ticketRepo.findById(ticketId);
    }

    /**
     * safe size for nullable lists
     * @param list the list
     * @return the list size or 0
     */
    private static int safeSize(final List<?> list) {
        return (list == null) ? 0 : list.size();
    }

    /**
     * returns the list or an empty list if null
     * @param list the list
     * @return a non-null list
     */
    private static List<String> safeListStrings(final List<String> list) {
        return (list == null) ? new ArrayList<>() : list;
    }

    /**
     * returns the list or an empty list if null
     * @param list the list
     * @return a non-null list
     */
    private static List<Integer> safeListInts(final List<Integer> list) {
        return (list == null) ? new ArrayList<>() : list;
    }

    /**
     * computes completion ratio and rounds to 2 decimals
     * @param closed the number of closed tickets
     * @param total the total number of tickets
     * @return the completion ratio
     */
    private static double completionRatio(final int closed, final int total) {
        if (total == 0) {
            return 0.0;
        }
        return round2((double) closed / (double) total);
    }

    /**
     * rounds a value to 2 decimals
     * @param v the value
     * @return the rounded value
     */
    private static double round2(final double v) {
        return Math.round(v * HUNDRED) / HUNDRED;
    }

    /**
     * checks if a developer is assigned to the milestone of a ticket
     * @param ticketId the ticket id
     * @param devUsername the developer username
     * @return true if assigned false otherwise
     */
    public boolean isDevAssignedToTicketMilestone(final int ticketId,
                                                  final String devUsername) {
        return milestoneRepo.isDevAssignedToTicketMilestone(ticketId, devUsername);
    }
}
