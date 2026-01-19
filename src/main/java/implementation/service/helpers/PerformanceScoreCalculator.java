package implementation.service.helpers;

import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.Ticket;
import implementation.domain.ticket.TicketStatus;
import implementation.domain.ticket.TicketType;
import implementation.domain.user.SeniorityLevel;
import implementation.domain.user.User;
import implementation.registry.TicketRegistry;
import implementation.registry.UserRegistry;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * helper used to compute monthly performance report rows for developers
 * based on closed tickets, priority mix, resolution time and seniority
 */
public final class PerformanceScoreCalculator {
    private static final double BONUS_JUNIOR = 5.0;
    private static final double BONUS_MID = 15.0;
    private static final double BONUS_SENIOR = 30.0;

    private static final double CLOSED = 0.5;
    private static final double HIGH_PRIO_MID = 0.7;
    private static final double RES_MID = 0.3;

    private static final double HIGH_PRIO_SENIOR = 1.0;
    private static final double RES_SENIOR = 0.5;

    private static final double ROUND_SCALE = 100.0;
    private static final double TYPE_COUNT = 3.0;

    private final TicketRegistry repo;
    private final UserRegistry userRepo;

    /**
     * creates the performance report calculator
     * @param repo the ticket registry
     * @param userRepo the user registry
     */
    public PerformanceScoreCalculator(final TicketRegistry repo,
                                      final UserRegistry userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    /**
     * generates the report
     * @param managerUsername the manager username
     * @param timestamp the command timestamp
     * @return list of rows for report
     */
    public List<Map<String, Object>> generate(final String managerUsername,
                                              final String timestamp) {
        User manager = userRepo.findByUsername(managerUsername);
        if (manager == null) {
            return Collections.emptyList();
        }

        LocalDate cmdDate = LocalDate.parse(timestamp);
        LocalDate prevMonth = cmdDate.minusMonths(1);

        int targetYear = prevMonth.getYear();
        int targetMonth = prevMonth.getMonthValue();

        List<String> devUsernames = new ArrayList<>(manager.getSubordinates());
        devUsernames.sort(String::compareTo);

        List<Map<String, Object>> out = new ArrayList<>();

        for (int i = 0; i < devUsernames.size(); i++) {
            String devUsername = devUsernames.get(i);

            User dev = userRepo.findByUsername(devUsername);
            if (dev == null) {
                continue;
            }

            List<Ticket> closed =
                    collectClosedTicketsForMonth(devUsername, targetYear, targetMonth);

            int closedCount = closed.size();
            double avgResTime = averageResolutionTime(closed);

            int highPriorityCount = 0;
            int bug = 0;
            int feature = 0;
            int ui = 0;

            for (int j = 0; j < closed.size(); j++) {
                Ticket t = closed.get(j);

                BusinessPriority bp = t.getBusinessPriority();
                if (bp == BusinessPriority.MEDIUM
                        || bp == BusinessPriority.HIGH
                        || bp == BusinessPriority.CRITICAL) {
                    highPriorityCount++;
                }

                TicketType type = t.getType();
                if (type == TicketType.BUG) {
                    bug++;
                } else if (type == TicketType.FEATURE_REQUEST) {
                    feature++;
                } else if (type == TicketType.UI_FEEDBACK) {
                    ui++;
                }
            }

            double score = performanceScore(
                    dev.getSeniorityLevel(),
                    closedCount,
                    highPriorityCount,
                    avgResTime,
                    bug,
                    feature,
                    ui
            );

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("username", devUsername);
            row.put("closedTickets", closedCount);
            row.put("averageResolutionTime", round2(avgResTime));
            row.put("performanceScore", round2(score));

            String seniorityStr = "";
            if (dev.getSeniorityLevel() != null) {
                seniorityStr = dev.getSeniorityLevel().name();
            }
            row.put("seniority", seniorityStr);

            out.add(row);
        }

        return out;
    }

    /**
     * collects tickets closed in a specific month
     * @param devUsername the developer username
     * @param year the year
     * @param month the month
     * @return list of closed tickets for month
     */
    private List<Ticket> collectClosedTicketsForMonth(final String devUsername,
                                                      final int year,
                                                      final int month) {
        List<Ticket> out = new ArrayList<>();
        List<Ticket> allTickets = repo.getAll();

        for (int i = 0; i < allTickets.size(); i++) {
            Ticket t = allTickets.get(i);

            if (!devUsername.equals(t.getAssignedTo())) {
                continue;
            }
            if (t.getStatus() != TicketStatus.CLOSED) {
                continue;
            }
            if (t.getClosedAt() == null) {
                continue;
            }

            LocalDate closedAt = t.getClosedAt();
            if (closedAt.getYear() == year && closedAt.getMonthValue() == month) {
                out.add(t);
            }
        }

        return out;
    }

    /**
     * computes average resolution time in days
     * @param tickets the tickets
     * @return average days
     */
    private double averageResolutionTime(final List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        int cnt = 0;

        for (int i = 0; i < tickets.size(); i++) {
            Ticket t = tickets.get(i);

            LocalDate a = t.getAssignedAtDate();
            LocalDate s = t.getSolvedAtDate();
            if (a == null || s == null) {
                continue;
            }

            long days = ChronoUnit.DAYS.between(a, s) + 1;
            if (days < 1) {
                days = 1;
            }

            sum += days;
            cnt++;
        }

        if (cnt == 0) {
            return 0.0;
        }
        return sum / cnt;
    }

    /**
     * computes performance score
     * @param level the seniority level
     * @param closedTickets the number of closed tickets
     * @param highPriorityTickets the number of high priority tickets
     * @param avgResTime the average resolution time
     * @param bug the number of bug tickets
     * @param feature the number of feature tickets
     * @param ui the number of ui feedback tickets
     * @return the computed score
     */
    private double performanceScore(final SeniorityLevel level,
                                    final int closedTickets,
                                    final int highPriorityTickets,
                                    final double avgResTime,
                                    final int bug,
                                    final int feature,
                                    final int ui) {
        if (level == null) {
            return 0.0;
        }
        if (closedTickets == 0) {
            return 0.0;
        }

        double seniorityBonus;
        if (level == SeniorityLevel.JUNIOR) {
            seniorityBonus = BONUS_JUNIOR;
        } else if (level == SeniorityLevel.MID) {
            seniorityBonus = BONUS_MID;
        } else {
            seniorityBonus = BONUS_SENIOR;
        }

        if (level == SeniorityLevel.JUNIOR) {
            double diversity = ticketDiversityFactor(bug, feature, ui);
            double base = Math.max(0.0, CLOSED * closedTickets - diversity);
            return base + seniorityBonus;
        }

        if (level == SeniorityLevel.MID) {
            double base = Math.max(
                    0.0,
                    CLOSED * closedTickets
                            + HIGH_PRIO_MID * highPriorityTickets
                            - RES_MID * avgResTime
            );
            return base + seniorityBonus;
        }

        double base = Math.max(
                0.0,
                CLOSED * closedTickets
                        + HIGH_PRIO_SENIOR * highPriorityTickets
                        - RES_SENIOR * avgResTime
        );
        return base + seniorityBonus;
    }

    /**
     * computes average resolved ticket type count
     * @param bug the bug count
     * @param feature the feature count
     * @param ui the ui count
     * @return the mean
     */
    private static double averageResolvedTicketType(final int bug,
                                                    final int feature,
                                                    final int ui) {
        return (bug + feature + ui) / TYPE_COUNT;
    }

    /**
     * computes standard deviation for ticket type counts
     * @param bug the bug count
     * @param feature the feature count
     * @param ui the ui count
     * @return the standard deviation
     */
    private static double standardDeviation(final int bug,
                                            final int feature,
                                            final int ui) {
        double mean = averageResolvedTicketType(bug, feature, ui);

        double sumSq =
                Math.pow(bug - mean, 2)
                        + Math.pow(feature - mean, 2)
                        + Math.pow(ui - mean, 2);

        double variance = sumSq / TYPE_COUNT;
        return Math.sqrt(variance);
    }

    /**
     * computes diversity factor for ticket types
     * @param bug the bug count
     * @param feature the feature count
     * @param ui the ui count
     * @return diversity factor
     */
    private static double ticketDiversityFactor(final int bug,
                                                final int feature,
                                                final int ui) {
        double mean = averageResolvedTicketType(bug, feature, ui);
        if (mean == 0.0) {
            return 0.0;
        }
        double std = standardDeviation(bug, feature, ui);
        return std / mean;
    }

    /**
     * rounds a value to 2 decimals
     * @param v the value
     * @return rounded value
     */
    private static double round2(final double v) {
        return Math.round(v * ROUND_SCALE) / ROUND_SCALE;
    }
}
