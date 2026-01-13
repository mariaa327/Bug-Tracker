package implementation.command.commands.ticket;

import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.dto.input.CommandInput;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.TicketService;

public class ReportTicketCommand implements Command {
    private final JsonUserRepository userRepo;
    private final TicketService ticketService;

    public ReportTicketCommand(JsonUserRepository userRepo, TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    @Override
    public CommandResult execute(CommandInput input) {
        if (userRepo.findByUsername(input.getUsername()).isEmpty())
            return CommandResult.error(input, "The user " + input.getUsername() + " does not exist.");

        String err = ticketService.reportTicket(input.getTimestamp(), input.getParams());
        if (err != null)
            return CommandResult.error(input, err);

        return null;
    }
}
