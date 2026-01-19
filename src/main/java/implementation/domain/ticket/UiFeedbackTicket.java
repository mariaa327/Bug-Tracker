package implementation.domain.ticket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import implementation.domain.user.ExpertiseArea;

import java.time.LocalDate;

public class UiFeedbackTicket extends Ticket {

    private static final double CUSTOMER_IMPACT_MAX = 100.0;
    private static final double RISK_MAX = 100.0;
    private static final double USABILITY_TOP = 11.0;
    private static final double EFFICIENCY_MAX = 20.0;

    private final BusinessValue businessValue;
    private final Integer usabilityScore;

    /**
     * creates a UI feedback ticket
     * @param title the ticket title
     * @param businessPriority the ticket business priority
     * @param createdAt the creation date
     * @param reportedBy the reporter username
     * @param expertiseArea the expertise area
     * @param description the ticket description
     * @param businessValue the business value
     * @param usabilityScore the usability score
     */
    public UiFeedbackTicket(final String title,
                            final BusinessPriority businessPriority,
                            final LocalDate createdAt,
                            final String reportedBy,
                            final ExpertiseArea expertiseArea,
                            final String description,
                            final BusinessValue businessValue,
                            final Integer usabilityScore) {

        super(TicketType.UI_FEEDBACK, title, businessPriority, createdAt, reportedBy,
                expertiseArea, description);
        this.businessValue = businessValue;
        this.usabilityScore = usabilityScore;
    }

    /**
     * returns the business value
     * @return the business value
     */
    @JsonIgnore
    public BusinessValue getBusinessValue() {
        return businessValue;
    }

    /**
     * returns the usability score
     * @return the usability score
     */
    @JsonIgnore
    public Integer getUsabilityScore() {
        return usabilityScore;
    }

    /**
     * computes the customer impact base score for this ticket
     * @return the base score
     */
    @Override
    protected double customerImpactBaseScore() {
        if (businessValue == null || usabilityScore == null) {
            return 0.0;
        }
        return (double) businessValue.score() * usabilityScore;
    }

    /**
     * returns the maximum value for the customer impact scoring
     * @return the maximum customer impact value
     */
    @Override
    protected double customerImpactMaxValue() {
        return CUSTOMER_IMPACT_MAX;
    }

    /**
     * computes the risk base score for this ticket
     * @return the base risk score
     */
    @Override
    protected double riskBaseScore() {
        if (getBusinessValue() == null || getUsabilityScore() == null) {
            return 0.0;
        }
        return (USABILITY_TOP - getUsabilityScore()) * getBusinessValue().score();
    }

    /**
     * returns the maximum value for the risk scoring
     * @return the maximum risk value
     */
    @Override
    protected double riskMaxValue() {
        return RISK_MAX;
    }

    /**
     * computes the efficiency base score for this ticket
     * @return the base efficiency score
     */
    @Override
    protected double efficiencyBaseScore() {
        if (getBusinessValue() == null || getUsabilityScore() == null) {
            return 0.0;
        }
        long d = daysToResolve();
        if (d == 0) {
            return 0.0;
        }
        return (getUsabilityScore() + getBusinessValue().score()) / (double) d;
    }

    /**
     * returns the maximum value for the efficiency scoring
     * @return the maximum efficiency value
     */
    @Override
    protected double efficiencyMaxValue() {
        return EFFICIENCY_MAX;
    }

}
