package implementation.command.commands.development;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.user.Role;
import implementation.dto.input.CommandInput;
import implementation.dto.input.InputLoader;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.TicketService;

public class UndoChangeStatusCommand implements Command {
    private final JsonUserRepository userRepo;
    private final TicketService ticketService;

    public UndoChangeStatusCommand(JsonUserRepository userRepo, TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    @Override
    public CommandResult execute(CommandInput input) {
        var userOpt = userRepo.findByUsername(input.getUsername());
        if (userOpt.isEmpty()) {
            return new CommandResult(errorNode(input, "User not found."));
        }

        Role role = userOpt.get().getRole();
        if (role != Role.DEVELOPER) {
            return new CommandResult(errorNode(input,
                    "required role DEVELOPER; user role " + role + "."));
        }

        String err = ticketService.undoChangeStatus(input.getUsername(), input.getTimestamp(), input.getTicketID());
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
