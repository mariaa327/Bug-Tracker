package implementation.domain.ticket;

import com.fasterxml.jackson.annotation.JsonIgnore;
import implementation.domain.user.ExpertiseArea;

import java.time.LocalDate;

public class BugTicket extends Ticket {

    private final Frequency frequency;
    private final Severity severity;

    public BugTicket(String title,
                     BusinessPriority businessPriority,
                     LocalDate createdAt,
                     String reportedBy,
                     ExpertiseArea expertiseArea,
                     String description) {
        this(title, businessPriority, createdAt, reportedBy, expertiseArea, description, null, null);
    }

    public BugTicket(String title,
                     BusinessPriority businessPriority,
                     LocalDate createdAt,
                     String reportedBy,
                     ExpertiseArea expertiseArea,
                     String description,
                     Frequency frequency,
                     Severity severity) {

        super(TicketType.BUG, title, businessPriority, createdAt, reportedBy, expertiseArea, description);
        this.frequency = frequency;
        this.severity = severity;
    }

    @JsonIgnore
    public Frequency getFrequency() {
        return frequency;
    }

    @JsonIgnore
    public Severity getSeverity() {
        return severity;
    }

    @Override
    protected double customerImpactBaseScore() {
        if (frequency == null || severity == null) return 0.0;
        return (double) frequency.score() * getBusinessPriority().score() * severity.score();
    }

    @Override
    protected double customerImpactMaxValue() {
        return 48.0;
    }

    @Override
    protected double riskBaseScore() {
        if (getFrequency() == null || getSeverity() == null) return 0.0;
        return (double) getFrequency().score() * getSeverity().score();
    }

    @Override
    protected double riskMaxValue() {
        return 12.0;
    }

    @Override
    protected double efficiencyBaseScore() {
        if (getFrequency() == null || getSeverity() == null) return 0.0;
        long d = daysToResolve();
        if (d == 0) return 0.0;
        return ((getFrequency().score() + getSeverity().score()) * 10.0) / d;
    }

    @Override
    protected double efficiencyMaxValue() {
        return 70.0;
    }
}
