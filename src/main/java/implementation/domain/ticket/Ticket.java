package implementation.domain.ticket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import implementation.domain.user.ExpertiseArea;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    private LocalDate lastResolvedOrClosedAt;

    private final List<Comment> comments = new ArrayList<>();
    private final List<Map<String, Object>> actions = new ArrayList<>();

    private static final double PERCENT_MAX = 100.0;
    private static final double ZERO = 0.0;
    private static final double TWO_DECIMALS = 100.0;
    private static final int MIN_DAYS = 1;

    /**
     * creates a ticket base instance
     * subclasses provide specific scoring implementations
     * @param type the ticket type
     * @param title the ticket title
     * @param businessPriority the business priority
     * @param createdAt the creation date
     * @param reportedBy the reporter username
     * @param expertiseArea the expertise area
     * @param description the ticket description
     */
    protected Ticket(final TicketType type,
                     final String title,
                     final BusinessPriority businessPriority,
                     final LocalDate createdAt,
                     final String reportedBy,
                     final ExpertiseArea expertiseArea,
                     final String description) {
        this.type = type;
        this.title = title;
        this.businessPriority = businessPriority;
        this.createdAt = createdAt;
        this.reportedBy = (reportedBy == null) ? "" : reportedBy;
        this.expertiseArea = expertiseArea;
        this.description = description;
    }

    /**
     * returns the ticket id
     * @return the ticket id
     */
    public int getId() {
        return id;
    }

    /**
     * sets the ticket id
     * @param id the ticket id
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * returns the ticket type
     * @return the ticket type
     */
    public TicketType getType() {
        return type;
    }

    /**
     * returns the ticket title
     * @return the ticket title
     */
    public String getTitle() {
        return title;
    }

    /**
     * returns the business priority
     * @return the business priority
     */
    public BusinessPriority getBusinessPriority() {
        return businessPriority;
    }

    /**
     * sets the business priority
     * @param bp the new business priority
     */
    public void setBusinessPriority(final BusinessPriority bp) {
        this.businessPriority = bp;
    }

    /**
     * returns the current ticket status
     * @return the ticket status
     */
    public TicketStatus getStatus() {
        return status;
    }

    /**
     * sets the current ticket status
     * @param status the new ticket status
     */
    public void setStatus(final TicketStatus status) {
        this.status = status;
    }

    /**
     * returns the creation date
     * @return the creation date
     */
    public LocalDate getCreatedAt() {
        return createdAt;
    }

    /**
     * returns the reporter username
     * @return the reporter username
     */
    public String getReportedBy() {
        return reportedBy;
    }

    /**
     * returns the expertise area
     * @return the expertise area
     */
    @JsonIgnore
    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    /**
     * returns the ticket description
     * @return the ticket description
     */
    @JsonIgnore
    public String getDescription() {
        return description;
    }

    /**
     * returns the username the ticket is assigned to
     * @return the assigned username or empty string
     */
    public String getAssignedTo() {
        return assignedTo;
    }

    /**
     * returns the assigned date as string
     * @return the assigned date string or empty string
     */
    public String getAssignedAt() {
        return assignedAt == null ? "" : assignedAt.toString();
    }

    /**
     * returns the solved timestamp as string
     * @return the solved timestamp or empty string
     */
    public String getSolvedAt() {
        return solvedAt == null ? "" : solvedAt;
    }

    /**
     * returns the comment list
     * @return the comments
     */
    public List<Comment> getComments() {
        return comments;
    }

    /**
     * assigns the ticket to a developer and sets status to in progress
     * @param devUsername the developer username
     * @param assignedDate the assignment date
     */
    public void assignTo(final String devUsername, final LocalDate assignedDate) {
        this.assignedTo = devUsername;
        this.assignedAt = assignedDate;
        this.status = TicketStatus.IN_PROGRESS;
    }

    /**
     * removes the assignment and resets status to open
     */
    public void unassign() {
        this.assignedTo = "";
        this.assignedAt = null;
        this.status = TicketStatus.OPEN;
    }

    /**
     * checks whether the ticket is currently assigned
     * @return true if assigned to someone
     */
    @JsonIgnore
    public boolean isAssigned() {
        return assignedTo != null && !assignedTo.isEmpty();
    }

    /**
     * adds a comment to this ticket
     * @param author the comment author
     * @param content the comment content
     * @param commentTimestamp the comment timestamp
     */
    public void addComment(final String author, final String content,
                           final String commentTimestamp) {
        comments.add(new Comment(author, content, commentTimestamp));
    }

    /**
     * checks whether the ticket reporter is anonymous
     * @return true if reporter is missing
     */
    @JsonIgnore
    public boolean isAnonymous() {
        return reportedBy == null || reportedBy.isEmpty();
    }

    /**
     * removes the last comment made by the given author if present
     * @param author the comment author
     * @return true if a comment was removed
     */
    public boolean undoLastCommentBy(final String author) {
        if (author == null) {
            return false;
        }

        for (int i = comments.size() - 1; i >= 0; i--) {
            if (author.equals(comments.get(i).getAuthor())) {
                comments.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * returns the action history list
     * @return the actions
     */
    @JsonIgnore
    public List<Map<String, Object>> getActions() {
        return actions;
    }

    /**
     * logs that this ticket was added to a milestone
     * @param milestone the milestone name
     * @param by the user performing the action
     * @param timestamp the timestamp
     */
    public void logAddedToMilestone(final String milestone, final String by,
                                    final String timestamp) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("milestone", milestone);
        a.put("by", by);
        a.put("timestamp", timestamp);
        a.put("action", "ADDED_TO_MILESTONE");
        actions.add(a);
    }

    /**
     * logs that this ticket was assigned
     * @param by the user performing the action
     * @param timestamp the timestamp
     */
    public void logAssigned(final String by, final String timestamp) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("by", by);
        a.put("timestamp", timestamp);
        a.put("action", "ASSIGNED");
        actions.add(a);
    }

    /**
     * logs a status change action
     * @param from the previous status
     * @param to the new status
     * @param by the user performing the action
     * @param timestamp the timestamp
     */
    public void logStatusChanged(final TicketStatus from,
                                 final TicketStatus to,
                                 final String by,
                                 final String timestamp) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("from", from.name());
        a.put("to", to.name());
        a.put("by", by);
        a.put("timestamp", timestamp);
        a.put("action", "STATUS_CHANGED");
        actions.add(a);
    }

    /**
     * returns the closed date
     * @return the closed date
     */
    @JsonIgnore
    public LocalDate getClosedAt() {
        return closedAt;
    }

    /**
     * sets the closed date
     * @param closedAt the closed date
     */
    @JsonIgnore
    public void setClosedAt(final LocalDate closedAt) {
        this.closedAt = closedAt;
    }

    /**
     * logs a de-assigned action
     * @param by the user performing the action
     * @param timestamp the timestamp
     */
    public void logDeAssigned(final String by, final String timestamp) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("by", by);
        a.put("timestamp", timestamp);
        a.put("action", "DE-ASSIGNED");
        actions.add(a);
    }

    /**
     * checks if this ticket is eligible for customer impact scoring
     * @return true if priority is not low
     */
    @JsonIgnore
    public boolean eligibleForCustomerImpact() {
        return businessPriority != BusinessPriority.LOW;
    }

    /**
     * returns the base customer impact score computed by the subclass
     * @return the base customer impact score
     */
    @JsonIgnore
    protected abstract double customerImpactBaseScore();

    /**
     * returns the max customer impact base score for normalization
     * @return the max customer impact base score
     */
    @JsonIgnore
    protected abstract double customerImpactMaxValue();

    /**
     * computes the customer impact score in percent
     * @return the customer impact score percent rounded to 2 decimals
     */
    @JsonIgnore
    public final double customerImpactScore() {
        double base = customerImpactBaseScore();
        double max = customerImpactMaxValue();
        double raw = (base * PERCENT_MAX) / max;
        return Math.min(PERCENT_MAX, Math.round(raw * TWO_DECIMALS) / TWO_DECIMALS);
    }

    /**
     * returns the base risk score computed by the subclass
     * @return the base risk score
     */
    @JsonIgnore
    protected abstract double riskBaseScore();

    /**
     * returns the max risk base score for normalization
     * @return the max risk base score
     */
    @JsonIgnore
    protected abstract double riskMaxValue();

    /**
     * computes the risk percent value
     * @return the risk percent rounded to 2 decimals
     */
    @JsonIgnore
    public final double riskPercent() {
        double max = riskMaxValue();
        if (max == ZERO) {
            return ZERO;
        }

        double p = (riskBaseScore() * PERCENT_MAX) / max;
        if (p > PERCENT_MAX) {
            p = PERCENT_MAX;
        }
        if (p < ZERO) {
            p = ZERO;
        }
        return Math.round(p * TWO_DECIMALS) / TWO_DECIMALS;
    }

    /**
     * returns the last resolved or closed date used for resolution calculations
     * @return the last resolved or closed date
     */
    @JsonIgnore
    public LocalDate getLastResolvedOrClosedAt() {
        return lastResolvedOrClosedAt;
    }

    /**
     * sets the last resolved or closed date used for resolution calculations
     * @param d the new date
     */
    @JsonIgnore
    public void setLastResolvedOrClosedAt(final LocalDate d) {
        this.lastResolvedOrClosedAt = d;
    }

    /**
     * returns the assignment date
     * @return the assignment date
     */
    @JsonIgnore
    public LocalDate getAssignedAtDate() {
        return assignedAt;
    }

    /**
     * returns the closed date
     * @return the closed date
     */
    @JsonIgnore
    public LocalDate getClosedAtDate() {
        return closedAt;
    }

    /**
     * computes the number of days between assignment and last resolve/close date
     * @return the number of days, minimum 1 when both dates exist
     */
    @JsonIgnore
    public long daysToResolve() {
        if (assignedAt == null || lastResolvedOrClosedAt == null) {
            return 0;
        }

        long days = ChronoUnit.DAYS.between(assignedAt, lastResolvedOrClosedAt) + 1;

        // dacă e aceeași zi => 1 zi
        return Math.max(MIN_DAYS, days);
    }

    /**
     * returns the base efficiency score computed by the subclass
     * @return the base efficiency score
     */
    @JsonIgnore
    protected abstract double efficiencyBaseScore();

    /**
     * returns the max efficiency base score for normalization
     * @return the max efficiency base score
     */
    @JsonIgnore
    protected abstract double efficiencyMaxValue();

    /**
     * computes the efficiency score in percent
     * @return the efficiency score percent rounded to 2 decimals
     */
    @JsonIgnore
    public final double efficiencyScore() {
        double max = efficiencyMaxValue();
        long d = daysToResolve();
        if (max == ZERO || d == 0) {
            return ZERO;
        }

        double p = (efficiencyBaseScore() * PERCENT_MAX) / max;
        if (p > PERCENT_MAX) {
            p = PERCENT_MAX;
        }
        if (p < ZERO) {
            p = ZERO;
        }
        return Math.round(p * TWO_DECIMALS) / TWO_DECIMALS;
    }

    /**
     * parses and returns the solved date
     * @return the solved date or null if missing
     */
    @JsonIgnore
    public LocalDate getSolvedAtDate() {
        if (solvedAt == null || solvedAt.isEmpty()) {
            return null;
        }
        return LocalDate.parse(solvedAt);
    }

    /**
     * sets the solved date as string
     * @param d the solved date
     */
    @JsonIgnore
    public void setSolvedAtDate(final LocalDate d) {
        this.solvedAt = (d == null) ? "" : d.toString();
    }
}
