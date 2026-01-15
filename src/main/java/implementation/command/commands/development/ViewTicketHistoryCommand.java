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

import java.util.List;

public class ViewTicketHistoryCommand implements Command {
    private final JsonUserRepository userRepo;
    private final TicketService ticketService;

    public ViewTicketHistoryCommand(JsonUserRepository userRepo, TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    @Override
    public CommandResult execute(CommandInput input) {
        String username = input.getUsername();

        var userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            return new CommandResult(errorNode(input, "User not found."));
        }

        Role role = userOpt.get().getRole();
        if (role != Role.DEVELOPER) {
            return new CommandResult(errorNode(input,
                    "required role DEVELOPER; user role " + role + "."));
        }

        List<Ticket> tickets = ticketService.viewTicketHistoryTickets(username);

        ObjectMapper mapper = InputLoader.getMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("command", input.getCommand());
        node.put("username", input.getUsername());
        node.put("timestamp", input.getTimestamp());

        ArrayNode arr = node.putArray("ticketHistory");
        for (Ticket t : tickets) {
            ObjectNode tn = mapper.createObjectNode();
            tn.put("id", t.getId());
            tn.put("title", t.getTitle());
            tn.put("status", t.getStatus().toString());
            tn.set("actions", mapper.valueToTree(t.getActions()));
            tn.set("comments", mapper.valueToTree(t.getComments()));
            arr.add(tn);
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
