package implementation.domain.milestone;

import java.time.LocalDate;
import java.util.List;

public class Milestone {
    private final String name;
    private final List<String> blockingFor;
    private final LocalDate dueDate;
    private final LocalDate createdAt;
    private final List<Integer> tickets;
    private final List<String> assignedDevs;
    private final String createdBy;

    public Milestone(String name, List<String> blockingFor, LocalDate dueDate,
                     LocalDate createdAt, List<Integer> tickets,
                     List<String> assignedDevs, String createdBy) {
        this.name = name;
        this.blockingFor = blockingFor;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.tickets = tickets;
        this.assignedDevs = assignedDevs;
        this.createdBy = createdBy;
    }

    public String getName() {
        return name;
    }
    public List<String> getBlockingFor() {
        return blockingFor;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public LocalDate getCreatedAt() {
        return createdAt;
    }
    public List<Integer> getTickets() {
        return tickets;
    }
    public List<String> getAssignedDevs() {
        return assignedDevs;
    }
    public String getCreatedBy() {
        return createdBy;
    }
}
