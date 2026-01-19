package implementation.domain.milestone;

import java.time.LocalDate;
import java.util.List;

/**
 * domain object that models a milestone and its metadata
 * (due date, assigned developers, linked tickets and blocking relations)
 */

public class Milestone {
    private final String name;
    private final List<String> blockingFor;
    private final LocalDate dueDate;
    private final LocalDate createdAt;
    private final List<Integer> tickets;
    private final List<String> assignedDevs;
    private final String createdBy;

    /**
     * creates the milestone
     * @param name the milestone name
     * @param blockingFor the milestone blocking list
     * @param dueDate the due date
     * @param createdAt the creation date
     * @param tickets the ticket ids
     * @param assignedDevs the assigned developers
     * @param createdBy the creator username
     */
    public Milestone(final String name, final List<String> blockingFor,
                     final LocalDate dueDate,
                     final LocalDate createdAt, final List<Integer> tickets,
                     final List<String> assignedDevs, final String createdBy) {
        this.name = name;
        this.blockingFor = blockingFor;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.tickets = tickets;
        this.assignedDevs = assignedDevs;
        this.createdBy = createdBy;
    }

    /**
     * returns the milestone name
     * @return the milestone name
     */
    public String getName() {
        return name;
    }

    /**
     * returns the milestone blocking list
     * @return the milestone blocking list
     */
    public List<String> getBlockingFor() {
        return blockingFor;
    }

    /**
     * returns the milestone due date
     * @return the milestone due date
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * returns the milestone creation date
     * @return the milestone creation date
     */
    public LocalDate getCreatedAt() {
        return createdAt;
    }

    /**
     * returns the milestone ticket ids
     * @return the milestone ticket ids
     */
    public List<Integer> getTickets() {
        return tickets;
    }

    /**
     * returns the assigned developers
     * @return the assigned developers
     */
    public List<String> getAssignedDevs() {
        return assignedDevs;
    }

    /**
     * returns the milestone creator username
     * @return the milestone creator username
     */
    public String getCreatedBy() {
        return createdBy;
    }
}
