package implementation.command.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.input.CommandInput;
import implementation.input.InputReader;
import implementation.registry.UserRegistry;
import implementation.service.TicketService;
import implementation.domain.user.User;

public final class AddCommentCommand implements Command {
    private final UserRegistry userRepo;
    private final TicketService ticketService;

    public AddCommentCommand(final UserRegistry userRepo, final TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    /**
     * executes the add comment command
     * @param input the command input
     * @return the command result
     */
    @Override
    public CommandResult execute(final CommandInput input) {
        User user = userRepo.findByUsername(input.getUsername());
        if (user == null) {
            return new CommandResult(errorNode(input, "User not found."));
        }

        Integer id = input.getTicketID();
        if (id == null) {
            return new CommandResult(null);
        }

        String err = ticketService.addComment(input.getUsername(),
                id, input.getComment(), input.getTimestamp());

        if (err != null) {
            return new CommandResult(errorNode(input, err));
        }

        return new CommandResult(null);
    }

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
