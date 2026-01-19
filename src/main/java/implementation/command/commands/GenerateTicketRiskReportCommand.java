package implementation.command.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.input.CommandInput;
import implementation.input.InputReader;
import implementation.strategy.ReportStrategy;
import implementation.strategy.TicketRiskReportStrategy;
import implementation.registry.UserRegistry;
import implementation.service.TicketService;

public final class GenerateTicketRiskReportCommand implements Command {

    private final UserRegistry userRepo;
    private final TicketService ticketService;

    /**
     * creates the generate ticket risk report command
     * @param userRepo the user repository
     * @param ticketService the ticket service
     */
    public GenerateTicketRiskReportCommand(final UserRegistry userRepo,
                                           final TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    /**
     * executes the generate ticket risk report command
     * the requesting user must exist
     * @param input the command input
     * @return an error result if validation fails success result otherwise
     */
    @Override
    public CommandResult execute(final CommandInput input) {
        if (userRepo.findByUsername(input.getUsername()) == null) {
            return CommandResult.error(input,
                    "The user " + input.getUsername() + " does not exist.");
        }

        ObjectMapper mapper = InputReader.getMapper();
        ObjectNode out = mapper.createObjectNode();
        out.put("command", input.getCommand());
        out.put("username", input.getUsername());
        out.put("timestamp", input.getTimestamp());

        ReportStrategy strategy = new TicketRiskReportStrategy();
        out.set("report", strategy.generate(ticketService.getAllTickets(), mapper));

        return CommandResult.success(out);
    }
}
