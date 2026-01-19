package implementation.command.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.ticket.Ticket;
import implementation.domain.user.Role;
import implementation.input.CommandInput;
import implementation.input.InputReader;
import implementation.registry.UserRegistry;
import implementation.service.TicketService;
import implementation.domain.user.User;

import java.util.List;

public class ViewTicketHistoryCommand implements Command {
    private final UserRegistry userRepo;
    private final TicketService ticketService;

    /**
     * creates the view ticket history command
     * @param userRepo the user repository
     * @param ticketService the ticket service
     */
    public ViewTicketHistoryCommand(final UserRegistry userRepo,
                                    final TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    /**
     * executes the view ticket history command
     * the requesting user must exist and must have the developer role
     * @param input the command input
     * @return a command result
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

        List<Ticket> tickets = ticketService.viewTicketHistoryTickets(username);

        ObjectMapper mapper = InputReader.getMapper();
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
