package implementation.domain.ticket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import implementation.domain.user.ExpertiseArea;

import java.time.LocalDate;

public class UiFeedbackTicket extends Ticket {

    private final BusinessValue businessValue;
    private final Integer usabilityScore;

    public UiFeedbackTicket(String title,
                            BusinessPriority businessPriority,
                            LocalDate createdAt,
                            String reportedBy,
                            ExpertiseArea expertiseArea,
                            String description) {
        this(title, businessPriority, createdAt, reportedBy, expertiseArea, description, null, null);
    }

    public UiFeedbackTicket(String title,
                            BusinessPriority businessPriority,
                            LocalDate createdAt,
                            String reportedBy,
                            ExpertiseArea expertiseArea,
                            String description,
                            BusinessValue businessValue,
                            Integer usabilityScore) {

        super(TicketType.UI_FEEDBACK, title, businessPriority, createdAt, reportedBy, expertiseArea, description);
        this.businessValue = businessValue;
        this.usabilityScore = usabilityScore;
    }

    @JsonIgnore
    public BusinessValue getBusinessValue() {
        return businessValue;
    }

    @JsonIgnore
    public Integer getUsabilityScore() {
        return usabilityScore;
    }

    @Override
    protected double customerImpactBaseScore() {
        if (businessValue == null || usabilityScore == null) return 0.0;
        return (double) businessValue.score() * usabilityScore;
    }

    @Override
    protected double customerImpactMaxValue() {
        return 100.0;
    }

    @Override
    protected double riskBaseScore() {
        if (getBusinessValue() == null || getUsabilityScore() == null) return 0.0;
        return (11.0 - getUsabilityScore()) * getBusinessValue().score();
    }

    @Override
    protected double riskMaxValue() {
        return 100.0;
    }

    @Override
    protected double efficiencyBaseScore() {
        if (getBusinessValue() == null || getUsabilityScore() == null) return 0.0;
        long d = daysToResolve();
        if (d == 0) return 0.0;
        return (getUsabilityScore() + getBusinessValue().score()) / (double) d;
    }

    @Override
    protected double efficiencyMaxValue() {
        return 20.0;
    }

}
