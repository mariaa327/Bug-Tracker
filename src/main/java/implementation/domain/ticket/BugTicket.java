package implementation.domain.ticket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import implementation.domain.user.ExpertiseArea;

import java.time.LocalDate;

public final class BugTicket extends Ticket {

    private static final double CUSTOMER_IMPACT_MAX = 48.0;
    private static final double RISK_MAX = 12.0;
    private static final double EFFICIENCY_WEIGHT = 10.0;
    private static final double EFFICIENCY_MAX = 70.0;

    private final Frequency frequency;
    private final Severity severity;

    /**
     * creates the bug ticket
     * @param title the ticket title
     * @param businessPriority the business priority
     * @param createdAt the creation date
     * @param reportedBy the reporter username
     * @param expertiseArea the expertise area
     * @param description the ticket description
     * @param frequency the bug frequency
     * @param severity the bug severity
     */
    public BugTicket(final String title,
                     final BusinessPriority businessPriority,
                     final LocalDate createdAt,
                     final String reportedBy,
                     final ExpertiseArea expertiseArea,
                     final String description,
                     final Frequency frequency,
                     final Severity severity) {

        super(TicketType.BUG, title, businessPriority,
                createdAt, reportedBy, expertiseArea, description);
        this.frequency = frequency;
        this.severity = severity;
    }

    /**
     * returns the bug frequency
     * @return the bug frequency
     */
    @JsonIgnore
    public Frequency getFrequency() {
        return frequency;
    }

    /**
     * returns the bug severity
     * @return the bug severity
     */
    @JsonIgnore
    public Severity getSeverity() {
        return severity;
    }

    /**
     * computes the customer impact base score for this ticket type
     * @return the customer impact base score
     */
    @Override
    protected double customerImpactBaseScore() {
        if (frequency == null || severity == null) {
            return 0.0;
        }
        return (double) frequency.score() * getBusinessPriority().score() * severity.score();
    }

    /**
     * returns the maximum possible customer impact value for this ticket type
     * @return the maximum customer impact value
     */
    @Override
    protected double customerImpactMaxValue() {
        return CUSTOMER_IMPACT_MAX;
    }

    /**
     * computes the risk base score for this ticket type
     * @return the risk base score
     */
    @Override
    protected double riskBaseScore() {
        if (getFrequency() == null || getSeverity() == null) {
            return 0.0;
        }
        return (double) getFrequency().score() * getSeverity().score();
    }

    /**
     * returns the maximum possible risk value for this ticket type
     * @return the maximum risk value
     */
    @Override
    protected double riskMaxValue() {
        return RISK_MAX;
    }

    /**
     * computes the efficiency base score for this ticket type
     * @return the efficiency base score
     */
    @Override
    protected double efficiencyBaseScore() {
        if (getFrequency() == null || getSeverity() == null) {
            return 0.0;
        }
        long d = daysToResolve();
        if (d == 0) {
            return 0.0;
        }
        return ((getFrequency().score() + getSeverity().score()) * EFFICIENCY_WEIGHT) / d;
    }

    /**
     * returns the maximum possible efficiency value for this ticket type
     * @return the maximum efficiency value
     */
    @Override
    protected double efficiencyMaxValue() {
        return EFFICIENCY_MAX;
    }
}
