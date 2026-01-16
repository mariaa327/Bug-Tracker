package implementation.domain.ticket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import implementation.domain.user.ExpertiseArea;

import java.time.LocalDate;

public class FeatureRequestTicket extends Ticket {

    private final BusinessValue businessValue;
    private final CustomerDemand customerDemand;

    public FeatureRequestTicket(String title,
                                BusinessPriority businessPriority,
                                LocalDate createdAt,
                                String reportedBy,
                                ExpertiseArea expertiseArea,
                                String description) {
        this(title, businessPriority, createdAt, reportedBy, expertiseArea, description, null, null);
    }

    public FeatureRequestTicket(String title,
                                BusinessPriority businessPriority,
                                LocalDate createdAt,
                                String reportedBy,
                                ExpertiseArea expertiseArea,
                                String description,
                                BusinessValue businessValue,
                                CustomerDemand customerDemand) {

        super(TicketType.FEATURE_REQUEST, title, businessPriority, createdAt, reportedBy, expertiseArea, description);
        this.businessValue = businessValue;
        this.customerDemand = customerDemand;
    }

    @JsonIgnore
    public BusinessValue getBusinessValue() {
        return businessValue;
    }

    @JsonIgnore
    public CustomerDemand getCustomerDemand() {
        return customerDemand;
    }

    @Override
    protected double customerImpactBaseScore() {
        if (businessValue == null || customerDemand == null) return 0.0;
        return (double) businessValue.score() * customerDemand.score();
    }

    @Override
    protected double customerImpactMaxValue() {
        return 100.0;
    }

    @Override
    protected double riskBaseScore() {
        if (getBusinessValue() == null || getCustomerDemand() == null) return 0.0;
        return (double) getBusinessValue().score() + getCustomerDemand().score();
    }

    @Override
    protected double riskMaxValue() {
        return 20.0;
    }

    @Override
    protected double efficiencyBaseScore() {
        if (getBusinessValue() == null || getCustomerDemand() == null) return 0.0;
        long d = daysToResolve();
        if (d == 0) return 0.0;
        return (getBusinessValue().score() + getCustomerDemand().score()) / (double) d;
    }

    @Override
    protected double efficiencyMaxValue() {
        return 20.0;
    }

}
