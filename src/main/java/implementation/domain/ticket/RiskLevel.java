package implementation.domain.ticket;

public enum RiskLevel {
    NEGLIGIBLE,
    MODERATE,
    SIGNIFICANT,
    MAJOR;

    private static final double THRESHOLD_NEGLIGIBLE = 24.0;
    private static final double THRESHOLD_MODERATE = 49.0;
    private static final double THRESHOLD_SIGNIFICANT = 74.0;

    /**
     * maps a percent value to a risk level
     * @param p the percent value
     * @return the corresponding risk level
     */
    public static RiskLevel fromPercent(final double p) {
        if (p <= THRESHOLD_NEGLIGIBLE) {
            return NEGLIGIBLE;
        }
        if (p <= THRESHOLD_MODERATE) {
            return MODERATE;
        }
        if (p <= THRESHOLD_SIGNIFICANT) {
            return SIGNIFICANT;
        }
        return MAJOR;
    }
}
