package implementation.command.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.input.CommandInput;
import implementation.input.InputReader;
import implementation.registry.UserRegistry;
import implementation.service.TicketService;

public final class UndoAddCommentCommand implements Command {
    private final UserRegistry userRepo;
    private final TicketService ticketService;

    /**
     * creates the undo add comment command
     * @param userRepo the user repository
     * @param ticketService the ticket service
     */
    public UndoAddCommentCommand(final UserRegistry userRepo, final TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    /**
     * executes the undo add comment command
     * the requesting user must exist
     * @param input the command input
     * @return an error result if validation fails null otherwise
     */
    @Override
    public CommandResult execute(final CommandInput input) {
        if (userRepo.findByUsername(input.getUsername()) == null) {
            return new CommandResult(errorNode(input, "User not found."));
        }

        Integer id = input.getTicketID();
        if (id == null) {
            return new CommandResult(null);
        }

        String err = ticketService.undoAddComment(
                input.getUsername(), id, input.getTimestamp()
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
    private ObjectNode errorNode(final CommandInput input, final String message) {
        ObjectMapper mapper = InputReader.getMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("command", input.getCommand());
        node.put("username", input.getUsername());
        node.put("timestamp", input.getTimestamp());
        node.put("error", message);
        return node;
    }
}
