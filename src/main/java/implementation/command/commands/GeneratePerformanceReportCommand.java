package implementation.command.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.user.Role;
import implementation.domain.user.User;
import implementation.input.CommandInput;
import implementation.input.InputReader;
import implementation.strategy.PerformanceReportStrategy;
import implementation.registry.UserRegistry;
import implementation.service.TicketService;

import java.util.List;
import java.util.Map;

public final class GeneratePerformanceReportCommand implements Command {
    private final UserRegistry userRepo;
    private final TicketService ticketService;

    /**
     * creates the generate performance report command
     * @param userRepo the user repository
     * @param ticketService the ticket service
     */
    public GeneratePerformanceReportCommand(final UserRegistry userRepo,
                                            final TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    /**
     * executes the generate performance report command
     * the requesting user must exist and must have the manager role
     * @param input the command input
     * @return an error result if validation fails success result otherwise
     */
    @Override
    public CommandResult execute(final CommandInput input) {
        final User user = userRepo.findByUsername(input.getUsername());
        if (user == null) {
            return CommandResult.error(input,
                    "The user " + input.getUsername() + " does not exist.");
        }

        if (user.getRole() != Role.MANAGER) {
            return CommandResult.error(
                    input,
                    "The user does not have permission to execute this command: "
                            + "required role MANAGER; user role "
                            + user.getRole()
                            + ".");
        }

        final PerformanceReportStrategy strategy = new PerformanceReportStrategy();
        final List<Map<String, Object>> report = strategy.generate(
                input.getUsername(),
                input.getTimestamp(),
                ticketService);

        final ObjectMapper mapper = InputReader.getMapper();
        final ObjectNode out = mapper.createObjectNode();
        out.put("command", input.getCommand());
        out.put("username", input.getUsername());
        out.put("timestamp", input.getTimestamp());
        out.set("report", mapper.valueToTree(report));

        return CommandResult.success(out);
    }
}
