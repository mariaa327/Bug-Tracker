package implementation.domain.ticket;

public enum CustomerDemand {
    LOW, MEDIUM, HIGH, VERY_HIGH;

    public int score() {
        return switch (this) {
            case LOW -> 1;
            case MEDIUM -> 3;
            case HIGH -> 6;
            case VERY_HIGH -> 10;
        };
    }

    public static CustomerDemand fromString(String s) {
        if (s == null) return null;
        String v = s.trim().toUpperCase();
        if ("CRITICAL".equals(v)) v = "VERY_HIGH";
        return CustomerDemand.valueOf(v);
    }
}
