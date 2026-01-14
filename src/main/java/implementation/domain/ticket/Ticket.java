package implementation.domain.ticket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import implementation.domain.user.ExpertiseArea;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Ticket {
    private int id;
    private TicketType type;
    private String title;
    private BusinessPriority businessPriority;
    private TicketStatus status = TicketStatus.OPEN;
    private LocalDate createdAt;

    private ExpertiseArea expertiseArea;
    private String description;
    private String reportedBy;

    private String assignedTo = "";
    private LocalDate assignedAt = null;
    private String solvedAt = "";
    private LocalDate closedAt;

    private final List<Comment> comments = new ArrayList<>();
    private final List<Map<String, Object>> actions = new ArrayList<>();

    protected Ticket(TicketType type,
                     String title,
                     BusinessPriority businessPriority,
                     LocalDate createdAt,
                     String reportedBy,
                     ExpertiseArea expertiseArea,
                     String description) {
        this.type = type;
        this.title = title;
        this.businessPriority = businessPriority;
        this.createdAt = createdAt;
        this.reportedBy = (reportedBy == null) ? "" : reportedBy;
        this.expertiseArea = expertiseArea;
        this.description = description;
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

    public String getReportedBy() {
        return reportedBy;
    }

    @JsonIgnore
    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    @JsonIgnore
    public String getDescription() {
        return description;
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

    public List<Comment> getComments() {
        return comments;
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

    @JsonIgnore
    public boolean isAssigned() {
        return assignedTo != null && !assignedTo.isEmpty();
    }

    @JsonIgnore
    public LocalDate getClosedAt() {
        return closedAt;
    }

    @JsonIgnore
    public void setClosedAt(LocalDate closedAt) {
        this.closedAt = closedAt;
    }

    @JsonIgnore
    public List<Map<String, Object>> getActions() {
        return actions;
    }

    public void logAddedToMilestone(String milestone, String by, String timestamp) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("milestone", milestone);
        a.put("by", by);
        a.put("timestamp", timestamp);
        a.put("action", "ADDED_TO_MILESTONE");
        actions.add(a);
    }

    public void addComment(String author, String content, String createdAt) {
        comments.add(new Comment(author, content, createdAt));
    }

    public boolean undoLastCommentBy(String author) {
        for (int i = comments.size() - 1; i >= 0; i--) {
            if (comments.get(i).getAuthor().equals(author)) {
                comments.remove(i);
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public LocalDate getAssignedAtDate() {
        return assignedAt;
    }

    @JsonIgnore
    public boolean isAnonymous() {
        return reportedBy == null || reportedBy.isEmpty();
    }

}
