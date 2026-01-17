package implementation.command.commands.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.user.Role;
import implementation.dto.input.CommandInput;
import implementation.dto.input.InputLoader;
import implementation.metrics.PerformanceReportStrategy;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.TicketService;

import java.util.List;
import java.util.Map;

public class GeneratePerformanceReportCommand implements Command {
    private final JsonUserRepository userRepo;
    private final TicketService ticketService;

    public GeneratePerformanceReportCommand(JsonUserRepository userRepo,
                                            TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    @Override
    public CommandResult execute(CommandInput input) {
        var uOpt = userRepo.findByUsername(input.getUsername());
        if (uOpt.isEmpty()) {
            return CommandResult.error(input, "The user " +
                    input.getUsername() + " does not exist.");
        }

        if (uOpt.get().getRole() != Role.MANAGER) {
            return CommandResult.error(input,
                    "The user does not have permission to execute this command: required role MANAGER; user role "
                            + uOpt.get().getRole() + ".");
        }

        PerformanceReportStrategy strategy = new PerformanceReportStrategy();
        List<Map<String, Object>> report = strategy.generate(
                input.getUsername(),
                input.getTimestamp(),
                ticketService
        );

        ObjectMapper mapper = InputLoader.getMapper();
        ObjectNode out = mapper.createObjectNode();
        out.put("command", input.getCommand());
        out.put("username", input.getUsername());
        out.put("timestamp", input.getTimestamp());
        out.set("report", mapper.valueToTree(report));

        return CommandResult.success(out);
    }
}
