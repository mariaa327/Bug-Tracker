package implementation.command.commands;

import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.user.Role;
import implementation.domain.user.User;
import implementation.input.CommandInput;
import implementation.registry.UserRegistry;
import implementation.service.TicketService;

public final class AssignTicketCommand implements Command {
    private final UserRegistry userRepo;
    private final TicketService ticketService;

    /**
     * creates a command that assigns a ticket to the requesting developer
     * @param userRepo the user repository used for user validation
     * @param ticketService the ticket service used to assign tickets
     */
    public AssignTicketCommand(final UserRegistry userRepo, final TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    /**
     * executes the assign ticket command
     * the requesting user must exist and must have the role
     * @param input the command input
     * @return an error result if validation fails null otherwise
     */
    @Override
    public CommandResult execute(final CommandInput input) {
        final User u = userRepo.findByUsername(input.getUsername());
        if (u == null) {
            return CommandResult.error(
                    input,
                    "The user " + input.getUsername() + " does not exist."
            );
        }

        if (u.getRole() != Role.DEVELOPER) {
            return CommandResult.error(
                    input,
                    "The user does not have permission to execute this command: "
                            + "required role DEVELOPER; user role " + u.getRole() + "."
            );
        }

        final Integer id = input.getTicketID();
        if (id == null) {
            return null;
        }

        final String err = ticketService.assignTicket(
                input.getUsername(),
                input.getTimestamp(),
                id
        );
        if (err != null) {
            return CommandResult.error(input, err);
        }

        return null;
    }
}
