package implementation.command.commands;

import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.input.CommandInput;
import implementation.registry.UserRegistry;
import implementation.service.TicketService;

public class ReportTicketCommand implements Command {
    private final UserRegistry userRepo;
    private final TicketService ticketService;

    /**
     * creates the report ticket command
     * @param userRepo the user repository
     * @param ticketService the ticket service
     */
    public ReportTicketCommand(final UserRegistry userRepo,
                               final TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    /**
     * executes the report ticket command
     * the requesting user must exist
     * @param input the command input
     * @return an error result if validation fails null otherwise
     */
    @Override
    public CommandResult execute(final CommandInput input) {
        if (userRepo.findByUsername(input.getUsername()) == null) {
            return CommandResult.error(input, "The user "
                    + input.getUsername() + " does not exist.");
        }

        String err = ticketService.reportTicket(input.getTimestamp(), input.getParams());
        if (err != null) {
            return CommandResult.error(input, err);
        }

        return null;
    }
}
