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

public class UndoAssignTicketCommand implements Command {
    private final UserRegistry userRepo;
    private final TicketService ticketService;

    public UndoAssignTicketCommand(final UserRegistry userRepo,
                                   final TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    /**
     * executes the undo assign ticket command
     * the requesting user must exist and must have the developer role
     * @param input the command input
     * @return an error result if validation fails null otherwise
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
            return new CommandResult(errorNode(input,
                    "required role DEVELOPER; user role " + role + "."));
        }

        Integer ticketId = input.getTicketID();
        if (ticketId == null) {
            return new CommandResult(errorNode(input, "Missing ticketID."));
        }

        String err = ticketService.undoAssignTicket(
                username,
                ticketId,
                input.getTimestamp()
        );
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
