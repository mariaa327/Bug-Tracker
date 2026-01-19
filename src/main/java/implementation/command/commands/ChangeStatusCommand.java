package implementation.command.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.user.Role;
import implementation.domain.user.User;
import implementation.input.CommandInput;
import implementation.input.InputReader;
import implementation.registry.UserRegistry;
import implementation.service.TicketService;

public final class ChangeStatusCommand implements Command {
    private final UserRegistry userRepo;
    private final TicketService ticketService;

    /**
     * creates a command that changes the status of a ticket
     * @param userRepo the user repository used for user validation
     * @param ticketService the ticket service used to change ticket status
     */
    public ChangeStatusCommand(final UserRegistry userRepo, final TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    /**
     * executes the change status command
     * the requesting user must exist and must have the role
     * @param input the command input
     * @return a commandResult containing an error payload on failure, otherwise
     * null output
     */
    @Override
    public CommandResult execute(final CommandInput input) {
        String username = input.getUsername();

        User user = userRepo.findByUsername(username);
        if (user == null) {
            return new CommandResult(errorNode(input, "User not found."));
        }

        Role role = user.getRole();
        if (role != Role.DEVELOPER) {
            return new CommandResult(errorNode(
                    input,
                    "required role DEVELOPER; user role " + role + "."
            ));
        }

        String err = ticketService.changeStatus(
                username,
                input.getTimestamp(),
                input.getTicketID()
        );
        if (err != null) {
            return new CommandResult(errorNode(input, err));
        }

        return new CommandResult(null);
    }

    /**
     * builds a standard error JSON node for the current command
     * @param input the command input
     * @param message the error message
     * @return a JSON object node containing the error payload
     */
    private ObjectNode errorNode(final CommandInput input, final String message) {
        final ObjectMapper mapper = InputReader.getMapper();
        final ObjectNode node = mapper.createObjectNode();
        node.put("command", input.getCommand());
        node.put("username", input.getUsername());
        node.put("timestamp", input.getTimestamp());
        node.put("error", message);
        return node;
    }
}
