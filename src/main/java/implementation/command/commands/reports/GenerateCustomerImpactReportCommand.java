package implementation.command.commands.reports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.dto.input.CommandInput;
import implementation.dto.input.InputLoader;
import implementation.metrics.CustomerImpactReportStrategy;
import implementation.metrics.ReportStrategy;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.TicketService;

public class GenerateCustomerImpactReportCommand implements Command {
    private final JsonUserRepository userRepo;
    private final TicketService ticketService;

    public GenerateCustomerImpactReportCommand(JsonUserRepository userRepo,
                                               TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    @Override
    public CommandResult execute(CommandInput input) {
        if (userRepo.findByUsername(input.getUsername()).isEmpty()) {
            return CommandResult.error(input, "The user " +
                    input.getUsername() + " does not exist.");
        }

        ObjectMapper mapper = InputLoader.getMapper();
        ObjectNode out = mapper.createObjectNode();
        out.put("command", input.getCommand());
        out.put("username", input.getUsername());
        out.put("timestamp", input.getTimestamp());

        ReportStrategy strategy = new CustomerImpactReportStrategy();
        out.set("report", strategy.generate(ticketService.getAllTickets(),
                mapper));

        return CommandResult.success(out);
    }
}
