package implementation.command.commands.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.dto.input.CommandInput;
import implementation.dto.input.InputLoader;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.TicketService;

public class UndoAddCommentCommand implements Command {
    private final JsonUserRepository userRepo;
    private final TicketService ticketService;

    public UndoAddCommentCommand(JsonUserRepository userRepo, TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    @Override
    public CommandResult execute(CommandInput input) {
        if (userRepo.findByUsername(input.getUsername()).isEmpty()) {
            return new CommandResult(errorNode(input, "User not found."));
        }

        Integer id = input.getTicketID();
        if (id == null) {
            return new CommandResult(null);
        }

        String err = ticketService.undoAddComment(
                input.getUsername(),
                id,
                input.getTimestamp()
        );

        if (err != null) {
            return new CommandResult(errorNode(input, err));
        }

        return new CommandResult(null);
    }

    private ObjectNode errorNode(CommandInput input, String message) {
        ObjectMapper mapper = InputLoader.getMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("command", input.getCommand());
        node.put("username", input.getUsername());
        node.put("timestamp", input.getTimestamp());
        node.put("error", message);
        return node;
    }
}
