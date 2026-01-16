package implementation.command.commands.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.CommandResult;
import implementation.command.Command;
import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.Ticket;
import implementation.domain.ticket.TicketStatus;
import implementation.domain.ticket.TicketType;
import implementation.dto.input.CommandInput;
import implementation.dto.input.InputLoader;
import implementation.repository.impl.JsonUserRepository;
import implementation.domain.user.Role;
import implementation.domain.user.User;
import implementation.service.TicketService;
import implementation.domain.flow.ProjectContext;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AppStabilityReportCommand implements Command {
    private final JsonUserRepository userRepo;
    private final TicketService ticketService;
    private final ProjectContext ctx;

    public AppStabilityReportCommand(JsonUserRepository userRepo, TicketService ticketService, ProjectContext ctx) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
        this.ctx = ctx;
    }

    @Override
    public CommandResult execute(CommandInput input) {
        Optional<User> uOpt = userRepo.findByUsername(input.getUsername());
        if (uOpt.isEmpty()) {
            return CommandResult.error(input, "User not found.");
        }
        if (uOpt.get().getRole() != Role.MANAGER) {
            return CommandResult.error(input, "User not allowed.");
        }

        List<Ticket> all = ticketService.getAllTickets();

        List<Ticket> open = all.stream()
                .filter(t -> t.getStatus() == TicketStatus.OPEN || t.getStatus() == TicketStatus.IN_PROGRESS)
                .toList();

        ObjectMapper mapper = InputLoader.getMapper();
        ObjectNode out = mapper.createObjectNode();
        out.put("command", input.getCommand());
        out.put("username", input.getUsername());
        out.put("timestamp", input.getTimestamp());

        ObjectNode report = mapper.createObjectNode();
        report.put("totalOpenTickets", open.size());

        Map<TicketType, Integer> byType = new EnumMap<>(TicketType.class);
        for (TicketType tt : TicketType.values()) byType.put(tt, 0);

        Map<BusinessPriority, Integer> byPrio = new EnumMap<>(BusinessPriority.class);
        for (BusinessPriority bp : BusinessPriority.values()) byPrio.put(bp, 0);

        for (Ticket t : open) {
            byType.put(t.getType(), byType.get(t.getType()) + 1);
            byPrio.put(t.getBusinessPriority(), byPrio.get(t.getBusinessPriority()) + 1);
        }

        ObjectNode byTypeNode = mapper.createObjectNode();
        for (TicketType tt : TicketType.values()) byTypeNode.put(tt.name(), byType.get(tt));
        report.set("openTicketsByType", byTypeNode);

        ObjectNode byPrioNode = mapper.createObjectNode();
        byPrioNode.put("LOW", byPrio.get(BusinessPriority.LOW));
        byPrioNode.put("MEDIUM", byPrio.get(BusinessPriority.MEDIUM));
        byPrioNode.put("HIGH", byPrio.get(BusinessPriority.HIGH));
        byPrioNode.put("CRITICAL", byPrio.get(BusinessPriority.CRITICAL));
        report.set("openTicketsByPriority", byPrioNode);

        ObjectNode riskByType = mapper.createObjectNode();
        for (TicketType tt : TicketType.values()) {
            double avg = averageRisk(open, tt);
            riskByType.put(tt.name(), riskGrade(avg));
        }
        report.set("riskByType", riskByType);

        ObjectNode impactByType = mapper.createObjectNode();

        for (TicketType type : new TicketType[]{TicketType.BUG, TicketType.FEATURE_REQUEST, TicketType.UI_FEEDBACK}) {
            double avg = open.stream()
                    .filter(t -> t.getType() == type)
                    .mapToDouble(Ticket::customerImpactScore)
                    .average()
                    .orElse(0.0);

            avg = Math.round(avg * 100.0) / 100.0;
            impactByType.put(type.name(), avg);
        }

        report.set("impactByType", impactByType);

        String stability = computeStability(open, riskByType, impactByType);
        report.put("appStability", stability);

        out.set("report", report);

        if ("STABLE".equals(stability)) {
            ctx.setInvestorsLost(true);
        }

        return CommandResult.success(out);
    }

    private static double averageRisk(List<Ticket> open, TicketType type) {
        double sum = 0.0;
        int count = 0;
        for (Ticket t : open) {
            if (t.getType() == type) {
                sum += t.riskPercent();
                count++;
            }
        }
        return count == 0 ? 0.0 : round2(sum / count);
    }

    private static double averageImpact(List<Ticket> open, TicketType type) {
        double sum = 0.0;
        int count = 0;
        for (Ticket t : open) {
            if (t.getType() == type && t.eligibleForCustomerImpact()) {
                sum += t.customerImpactScore();
                count++;
            }
        }
        return count == 0 ? 0.0 : round2(sum / count);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static String riskGrade(double riskPercent) {
        if (riskPercent <= 24.0) return "NEGLIGIBLE";
        if (riskPercent <= 49.0) return "MODERATE";
        if (riskPercent <= 74.0) return "SIGNIFICANT";
        return "MAJOR";
    }

    private static String computeStability(List<Ticket> open, ObjectNode riskByType, ObjectNode impactByType) {
        if (open.isEmpty()) return "STABLE";

        for (TicketType tt : TicketType.values()) {
            String r = riskByType.get(tt.name()).asText();
            if ("SIGNIFICANT".equals(r) || "MAJOR".equals(r)) return "UNSTABLE";
        }

        boolean allNegligible = true;
        for (TicketType tt : TicketType.values()) {
            if (!"NEGLIGIBLE".equals(riskByType.get(tt.name()).asText())) {
                allNegligible = false;
                break;
            }
        }
        boolean allImpactUnder50 = true;
        for (TicketType tt : TicketType.values()) {
            double imp = impactByType.get(tt.name()).asDouble();
            if (imp >= 50.0) {
                allImpactUnder50 = false;
                break;
            }
        }
        if (allNegligible && allImpactUnder50) return "STABLE";

        return "PARTIALLY STABLE";
    }
}
