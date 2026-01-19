package implementation.domain.ticket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import implementation.domain.user.ExpertiseArea;

import java.time.LocalDate;

public final class FeatureRequestTicket extends Ticket {

    private static final double CUSTOMER_IMPACT_MAX = 100.0;
    private static final double RISK_MAX = 20.0;
    private static final double EFFICIENCY_MAX = 20.0;

    private final BusinessValue businessValue;
    private final CustomerDemand customerDemand;

    /**
     * creates the feature request ticket
     * @param title the ticket title
     * @param businessPriority the business priority
     * @param createdAt the creation date
     * @param reportedBy the reporter username
     * @param expertiseArea the expertise area
     * @param description the ticket description
     * @param businessValue the business value
     * @param customerDemand the customer demand
     */
    public FeatureRequestTicket(final String title,
                                final BusinessPriority businessPriority,
                                final LocalDate createdAt,
                                final String reportedBy,
                                final ExpertiseArea expertiseArea,
                                final String description,
                                final BusinessValue businessValue,
                                final CustomerDemand customerDemand) {

        super(TicketType.FEATURE_REQUEST, title, businessPriority, createdAt,
                reportedBy, expertiseArea, description);
        this.businessValue = businessValue;
        this.customerDemand = customerDemand;
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
     * returns the customer demand
     * @return the customer demand
     */
    @JsonIgnore
    public CustomerDemand getCustomerDemand() {
        return customerDemand;
    }

    /**
     * computes the customer impact base score for this ticket type
     * @return the customer impact base score
     */
    @Override
    protected double customerImpactBaseScore() {
        if (businessValue == null || customerDemand == null) {
            return 0.0;
        }
        return (double) businessValue.score() * customerDemand.score();
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
        if (getBusinessValue() == null || getCustomerDemand() == null) {
            return 0.0;
        }
        return (double) getBusinessValue().score() + getCustomerDemand().score();
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
        if (getBusinessValue() == null || getCustomerDemand() == null) {
            return 0.0;
        }
        long d = daysToResolve();
        if (d == 0) {
            return 0.0;
        }
        return (getBusinessValue().score() + getCustomerDemand().score()) / (double) d;
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
