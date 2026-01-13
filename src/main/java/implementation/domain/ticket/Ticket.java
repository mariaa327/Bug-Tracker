package implementation.domain.ticket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Ticket {
    private int id;
    private final TicketType type;
    private final String title;
    private BusinessPriority businessPriority;
    private TicketStatus status = TicketStatus.OPEN;
    private final LocalDate createdAt;

    private final String reportedBy;

    private String assignedTo = "";
    private LocalDate assignedAt = null;
    private String solvedAt = "";

    private final List<Comment> comments = new ArrayList<>();

    protected Ticket(TicketType type,
                     String title,
                     BusinessPriority businessPriority,
                     LocalDate createdAt,
                     String reportedBy) {
        this.type = type;
        this.title = title;
        this.businessPriority = businessPriority;
        this.createdAt = createdAt;
        this.reportedBy = (reportedBy == null) ? "" : reportedBy;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public TicketType getType() {
        return type;
    }
    public String getTitle() {
        return title;
    }

    public BusinessPriority getBusinessPriority() {
        return businessPriority;
    }
    public void setBusinessPriority(BusinessPriority bp) {
        this.businessPriority = bp;
    }

    public TicketStatus getStatus() {
        return status;
    }
    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public String getAssignedTo() {
        return assignedTo;
    }
    public String getAssignedAt() {
        return assignedAt == null ? "" : assignedAt.toString();
    }
    public String getSolvedAt() {
        return solvedAt == null ? "" : solvedAt;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public List<Comment> getComments() {
        return comments;
    }

    @JsonIgnore
    public boolean isAnonymous() {
        return reportedBy == null || reportedBy.isEmpty();
    }

    public void assignTo(String devUsername, LocalDate date) {
        this.assignedTo = devUsername;
        this.assignedAt = date;
        this.status = TicketStatus.IN_PROGRESS;
    }

    public void unassign() {
        this.assignedTo = "";
        this.assignedAt = null;
        this.status = TicketStatus.OPEN;
    }
}
