package implementation.command.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.flow.ProjectContext;
import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.RiskLevel;
import implementation.domain.ticket.Ticket;
import implementation.domain.ticket.TicketStatus;
import implementation.domain.ticket.TicketType;
import implementation.domain.user.Role;
import implementation.domain.user.User;
import implementation.input.CommandInput;
import implementation.input.InputReader;
import implementation.registry.UserRegistry;
import implementation.service.TicketService;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * command used to generate an application stability report based on
 * currently open tickets
 */
public final class AppStabilityReportCommand implements Command {
    private static final double IMPACT_THRESHOLD = 50.0;
    private static final double ROUND_SCALE = 100.0;

    private final UserRegistry userRepo;
    private final TicketService ticketService;
    private final ProjectContext ctx;

    /**
     * creates a new command used to generate the application stability report
     * @param userRepo the user repository used for user validation
     * @param ticketService the ticket service used to fetch and aggregate ticket data
     * @param ctx the project context updated based on the computed stability
     */
    public AppStabilityReportCommand(final UserRegistry userRepo,
                                     final TicketService ticketService,
                                     final ProjectContext ctx) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
        this.ctx = ctx;
    }

    /**
     * generates an application stability report based on currently open
     * tickets
     * only MANAGER users are allowed to execute this command
     * @param input the command input
     * @return the command result containing the report JSON or an error JSON
     */
    @Override
    public CommandResult execute(final CommandInput input) {
        User user = userRepo.findByUsername(input.getUsername());
        if (user == null) {
            return CommandResult.error(input, "User not found.");
        }

        if (user.getRole() != Role.MANAGER) {
            return CommandResult.error(input, "User not allowed.");
        }

        List<Ticket> allTickets = ticketService.getAllTickets();
        List<Ticket> openTickets = getOpenTickets(allTickets);

        ObjectMapper mapper = InputReader.getMapper();
        ObjectNode out = baseOutput(mapper, input);

        ObjectNode report = mapper.createObjectNode();
        report.put("totalOpenTickets", openTickets.size());

        Map<TicketType, Integer> openByType = initTypeMap();
        Map<BusinessPriority, Integer> openByPriority = initPriorityMap();

        countOpenTickets(openTickets, openByType, openByPriority);

        report.set("openTicketsByType", toTypeNode(mapper, openByType));
        report.set("openTicketsByPriority", toPriorityNode(mapper, openByPriority));

        ObjectNode riskByType = buildRiskByType(mapper, openTickets);
        report.set("riskByType", riskByType);

        ObjectNode impactByType = buildImpactByType(mapper, openTickets);
        report.set("impactByType", impactByType);

        String stability = computeStability(openTickets, riskByType, impactByType);
        report.put("appStability", stability);

        out.set("report", report);

        if ("STABLE".equals(stability)) {
            ctx.setInvestorsLost(true);
        }

        return CommandResult.success(out);
    }

    /**
     * filters all tickets and returns only those that are considered "open"
     * @param all the list of all tickets
     * @return a list containing only open or in-progress tickets
     */
    private List<Ticket> getOpenTickets(final List<Ticket> all) {
        List<Ticket> open = new ArrayList<>();
        for (Ticket t : all) {
            if (t.getStatus() == TicketStatus.OPEN
                    || t.getStatus() == TicketStatus.IN_PROGRESS) {
                open.add(t);
            }
        }
        return open;
    }

    /**
     * builds the base output node containing standard fields (command,
     * username, timestamp)
     * @param mapper the object mapper used to create JSON nodes
     * @param input  the command input
     * @return a JSON node containing the base output fields
     */
    private ObjectNode baseOutput(final ObjectMapper mapper, final CommandInput input) {
        ObjectNode out = mapper.createObjectNode();
        out.put("command", input.getCommand());
        out.put("username", input.getUsername());
        out.put("timestamp", input.getTimestamp());
        return out;
    }

    /**
     * initializes a map for counting tickets by TicketType, with all keys
     * set to 0
     * @return a map containing all ticket types as keys initialized to 0
     */
    private Map<TicketType, Integer> initTypeMap() {
        Map<TicketType, Integer> m = new EnumMap<>(TicketType.class);
        for (TicketType tt : TicketType.values()) {
            m.put(tt, 0);
        }
        return m;
    }

    /**
     * Initializes a map for counting tickets by BusinessPriority, with all keys set to 0
     * @return a map containing all priorities as keys initialized to 0
     */
    private Map<BusinessPriority, Integer> initPriorityMap() {
        Map<BusinessPriority, Integer> m = new EnumMap<>(BusinessPriority.class);
        for (BusinessPriority bp : BusinessPriority.values()) {
            m.put(bp, 0);
        }
        return m;
    }

    /**
     * counts open tickets and increments the provided maps by type and by
     * priority
     * @param open the list of open tickets
     * @param byType the output map that counts tickets by type
     * @param byPriority the output map that counts tickets by priority
     */
    private void countOpenTickets(final List<Ticket> open,
                                  final Map<TicketType, Integer> byType,
                                  final Map<BusinessPriority, Integer> byPriority) {
        for (final Ticket t : open) {
            TicketType type = t.getType();
            BusinessPriority prio = t.getBusinessPriority();

            byType.put(type, byType.get(type) + 1);
            byPriority.put(prio, byPriority.get(prio) + 1);
        }
    }

    /**
     * converts a map of ticket type counts into a JSON object node
     * @param mapper the object mapper used to create JSON nodes
     * @param byType the map of counts by ticket type
     * @return a JSON node where each field name is a ticket type and the value is its count
     */
    private ObjectNode toTypeNode(final ObjectMapper mapper,
                                  final Map<TicketType, Integer> byType) {
        ObjectNode node = mapper.createObjectNode();
        for (TicketType tt : TicketType.values()) {
            node.put(tt.name(), byType.get(tt));
        }
        return node;
    }

    /**
     * converts a map of priority counts into a JSON object node
     * @param mapper the object mapper used to create JSON nodes
     * @param byPriority the map of counts by priority
     * @return a JSON node where each field name is a priority and the value is its count
     */
    private ObjectNode toPriorityNode(final ObjectMapper mapper,
                                      final Map<BusinessPriority, Integer> byPriority) {
        ObjectNode node = mapper.createObjectNode();
        for (BusinessPriority bp : BusinessPriority.values()) {
            node.put(bp.name(), byPriority.get(bp));
        }
        return node;
    }

    /**
     * computes an average risk percentage for each ticket type and converts
     * it into a risk level
     * @param mapper the object mapper used to create JSON nodes
     * @param open the list of open tickets
     * @return a JSON node mapping each ticket type to its computed RiskLevel
     */
    private ObjectNode buildRiskByType(final ObjectMapper mapper, final List<Ticket> open) {
        ObjectNode node = mapper.createObjectNode();

        for (TicketType type : TicketType.values()) {
            double sum = 0.0;
            int cnt = 0;

            for (final Ticket t : open) {
                if (t.getType() == type) {
                    sum += t.riskPercent();
                    cnt++;
                }
            }

            double avg = (cnt == 0) ? 0.0 : sum / cnt;
            node.put(type.name(), RiskLevel.fromPercent(avg).name());
        }

        return node;
    }

    /**
     * computes an average customer impact score for each ticket type and
     * rounds it to 2 decimals
     * @param mapper the object mapper used to create JSON nodes
     * @param open the list of open tickets
     * @return a JSON node mapping each ticket type to its average
     * impact score (rounded)
     */
    private ObjectNode buildImpactByType(final ObjectMapper mapper, final List<Ticket> open) {
        ObjectNode node = mapper.createObjectNode();

        for (TicketType type : TicketType.values()) {
            double sum = 0.0;
            int cnt = 0;

            for (final Ticket t : open) {
                if (t.getType() == type) {
                    sum += t.customerImpactScore();
                    cnt++;
                }
            }

            double avg = (cnt == 0) ? 0.0 : sum / cnt;
            node.put(type.name(), round2(avg));
        }

        return node;
    }

    /**
     * computes the application stability based on ticket risk levels and
     * impact scores
     * @param open the list of open tickets
     * @param riskByType a JSON node mapping ticket types to risk levels
     * @param impactByType a JSON node mapping ticket types to impact scores
     * @return the computed stability label
     */
    private static String computeStability(final List<Ticket> open,
                                           final ObjectNode riskByType,
                                           final ObjectNode impactByType) {
        if (open.isEmpty()) {
            return "STABLE";
        }

        for (TicketType tt : TicketType.values()) {
            String risk = riskByType.get(tt.name()).asText();
            if ("SIGNIFICANT".equals(risk) || "MAJOR".equals(risk)) {
                return "UNSTABLE";
            }
        }

        boolean allNegligible = true;
        for (TicketType tt : TicketType.values()) {
            if (!"NEGLIGIBLE".equals(riskByType.get(tt.name()).asText())) {
                allNegligible = false;
                break;
            }
        }

        boolean allImpactUnderThreshold = true;
        for (TicketType tt : TicketType.values()) {
            double imp = impactByType.get(tt.name()).asDouble();
            if (imp >= IMPACT_THRESHOLD) {
                allImpactUnderThreshold = false;
                break;
            }
        }

        if (allNegligible && allImpactUnderThreshold) {
            return "STABLE";
        }
        return "PARTIALLY STABLE";
    }

    /**
     * rounds a value to 2 decimals using the configured scale
     * @param v the value to round
     * @return the rounded value
     */
    private static double round2(final double v) {
        return Math.round(v * ROUND_SCALE) / ROUND_SCALE;
    }
}
