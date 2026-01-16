package implementation.domain.ticket;

public enum RiskLevel {
    NEGLIGIBLE, MODERATE, SIGNIFICANT, MAJOR;

    public static RiskLevel fromPercent(double p) {
        if (p <= 24.0) return NEGLIGIBLE;
        if (p <= 49.0) return MODERATE;
        if (p <= 74.0) return SIGNIFICANT;
        return MAJOR;
    }
}
