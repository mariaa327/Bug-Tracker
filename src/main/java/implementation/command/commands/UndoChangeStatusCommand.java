package implementation.command.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.user.Role;
import implementation.input.CommandInput;
import implementation.input.InputReader;
import implementation.registry.UserRegistry;
import implementation.service.TicketService;
import implementation.domain.user.User;

public class UndoChangeStatusCommand implements Command {
    private final UserRegistry userRepo;
    private final TicketService ticketService;

    /**
     * creates the undo change status command
     * @param userRepo the user repository
     * @param ticketService the ticket service
     */
    public UndoChangeStatusCommand(final UserRegistry userRepo,
                                   final TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    /**
     * executes the undo change status command
     * the requesting user must exist and must have the developer role
     * @param input the command input
     * @return an error result if validation fails null otherwise
     */
    @Override
    public CommandResult execute(final CommandInput input) {
        User user = userRepo.findByUsername(input.getUsername());
        if (user == null) {
            return new CommandResult(errorNode(input, "User not found."));
        }

        Role role = user.getRole();
        if (role != Role.DEVELOPER) {
            return new CommandResult(errorNode(input,
                    "required role DEVELOPER; user role " + role + "."));
        }

        String err = ticketService.undoChangeStatus(
                input.getUsername(),
                input.getTimestamp(),
                input.getTicketID());
        if (err != null) {
            return new CommandResult(errorNode(input, err));
        }

        return new CommandResult(null);
    }

    /**
     * builds the error node
     * @param input the command input
     * @param message the error message
     * @return the error node
     */
    private ObjectNode errorNode(final CommandInput input,
                                 final String message) {
        ObjectMapper mapper = InputReader.getMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("command", input.getCommand());
        node.put("username", input.getUsername());
        node.put("timestamp", input.getTimestamp());
        node.put("error", message);
        return node;
    }
}
