package implementation.domain.ticket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import implementation.domain.user.ExpertiseArea;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
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
    private LocalDate lastResolvedOrClosedAt;

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

    public void addComment(String author, String content, String createdAt) {
        comments.add(new Comment(author, content, createdAt));
    }

    @JsonIgnore
    public boolean isAnonymous() {
        return reportedBy == null || reportedBy.isEmpty();
    }

    public boolean undoLastCommentBy(String author) {
        if (author == null) return false;

        for (int i = comments.size() - 1; i >= 0; i--) {
            if (author.equals(comments.get(i).getAuthor())) {
                comments.remove(i);
                return true;
            }
        }
        return false;
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

    public void logAssigned(String by, String timestamp) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("by", by);
        a.put("timestamp", timestamp);
        a.put("action", "ASSIGNED");
        actions.add(a);
    }

    public void logStatusChanged(TicketStatus from, TicketStatus to, String by, String timestamp) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("from", from.name());
        a.put("to", to.name());
        a.put("by", by);
        a.put("timestamp", timestamp);
        a.put("action", "STATUS_CHANGED");
        actions.add(a);
    }

    @JsonIgnore
    public LocalDate getClosedAt() {
        return closedAt;
    }

    @JsonIgnore
    public void setClosedAt(LocalDate closedAt) {
        this.closedAt = closedAt;
    }

    public void logDeAssigned(String by, String timestamp) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("by", by);
        a.put("timestamp", timestamp);
        a.put("action", "DE-ASSIGNED");
        actions.add(a);
    }

    @JsonIgnore
    public boolean eligibleForCustomerImpact() {
        return businessPriority != BusinessPriority.LOW;
    }

    @JsonIgnore
    protected abstract double customerImpactBaseScore();

    @JsonIgnore
    protected abstract double customerImpactMaxValue();

    @JsonIgnore
    public final double customerImpactScore() {
        double base = customerImpactBaseScore();
        double max = customerImpactMaxValue();
        double raw = (base * 100.0) / max;
        return Math.min(100.0, Math.round(raw * 100.0) / 100.0);
    }

    @JsonIgnore
    protected abstract double riskBaseScore();

    @JsonIgnore
    protected abstract double riskMaxValue();

    @JsonIgnore
    public final double riskPercent() {
        double max = riskMaxValue();
        if (max == 0.0) return 0.0;

        double p = (riskBaseScore() * 100.0) / max;
        if (p > 100.0) p = 100.0;
        if (p < 0.0) p = 0.0;
        return Math.round(p * 100.0) / 100.0;
    }

    @JsonIgnore
    public LocalDate getLastResolvedOrClosedAt() {
        return lastResolvedOrClosedAt;
    }

    @JsonIgnore
    public void setLastResolvedOrClosedAt(LocalDate d) {
        this.lastResolvedOrClosedAt = d;
    }

    @JsonIgnore
    public LocalDate getAssignedAtDate() {
        return assignedAt;
    }

    @JsonIgnore
    public LocalDate getClosedAtDate() {
        return closedAt;
    }

    @JsonIgnore
    public long daysToResolve() {
        if (assignedAt == null || lastResolvedOrClosedAt == null) return 0;

        long days = ChronoUnit.DAYS.between(assignedAt,
                lastResolvedOrClosedAt) +1;

        // dacă e aceeași zi => 1 zi
        return Math.max(1, days);
    }

    @JsonIgnore
    protected abstract double efficiencyBaseScore();

    @JsonIgnore
    protected abstract double efficiencyMaxValue();

    @JsonIgnore
    public final double efficiencyScore() {
        double max = efficiencyMaxValue();
        long d = daysToResolve();
        if (max == 0.0 || d == 0) return 0.0;

        double p = (efficiencyBaseScore() * 100.0) / max;
        if (p > 100.0) p = 100.0;
        if (p < 0.0) p = 0.0;
        return Math.round(p * 100.0) / 100.0;
    }

    @JsonIgnore
    public LocalDate getSolvedAtDate() {
        if (solvedAt == null || solvedAt.isEmpty()) return null;
        return LocalDate.parse(solvedAt);
    }

    @JsonIgnore
    public void setSolvedAtDate(LocalDate d) {
        this.solvedAt = (d == null) ? "" : d.toString();
    }

}
