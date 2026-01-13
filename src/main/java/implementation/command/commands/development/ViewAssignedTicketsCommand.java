package implementation.command.commands.development;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.ticket.Ticket;
import implementation.domain.user.Role;
import implementation.dto.input.CommandInput;
import implementation.dto.input.InputLoader;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.TicketService;
import implementation.util.JsonOutputUtils;

import java.util.List;

public class ViewAssignedTicketsCommand implements Command {
    private final JsonUserRepository userRepo;
    private final TicketService ticketService;

    public ViewAssignedTicketsCommand(JsonUserRepository userRepo, TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    @Override
    public CommandResult execute(CommandInput input) {
        String username = input.getUsername();

        var userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty())
            return new CommandResult(errorNode(input, "The user " + username + " does not exist."));

        Role role = userOpt.get().getRole();
        if (role != Role.DEVELOPER)
            return new CommandResult(errorNode(input,
                    "required role DEVELOPER; user role " + role + "."));

        List<Ticket> assigned = ticketService.viewAssignedTickets(username, input.getTimestamp());

        ObjectMapper mapper = InputLoader.getMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("command", input.getCommand());
        node.put("username", username);
        node.put("timestamp", input.getTimestamp());

        ArrayNode arr = node.putArray("assignedTickets");
        for (Ticket t : assigned) {
            arr.add(JsonOutputUtils.ticketForAssignedTickets(mapper, t));
        }

        return new CommandResult(node);
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
