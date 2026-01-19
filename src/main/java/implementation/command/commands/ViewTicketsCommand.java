package implementation.command.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.ticket.Ticket;
import implementation.domain.user.Role;
import implementation.domain.user.User;
import implementation.input.CommandInput;
import implementation.input.InputReader;
import implementation.registry.UserRegistry;
import implementation.service.TicketService;

import java.util.List;

public class ViewTicketsCommand implements Command {
    private final UserRegistry userRepo;
    private final TicketService ticketService;

    /**
     * creates the view tickets command
     * @param userRepo the user repository
     * @param ticketService the ticket service
     */
    public ViewTicketsCommand(final UserRegistry userRepo,
                              final TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    /**
     * executes the view tickets command
     * the requesting user must exist
     * @param input the command input
     * @return an error result if validation fails success result otherwise
     */
    @Override
    public CommandResult execute(final CommandInput input) {
        User user = userRepo.findByUsername(input.getUsername());
        if (user == null) {
            return CommandResult.error(input,
                    "The user " + input.getUsername() + " does not exist.");
        }

        List<Ticket> tickets;
        if (user.getRole() == Role.MANAGER) {
            tickets = ticketService.viewTicketsAsManager(input.getTimestamp());
        } else if (user.getRole() == Role.DEVELOPER) {
            tickets = ticketService.viewTicketsAsDeveloper(user.getUsername(),
                    input.getTimestamp());
        } else {
            tickets = ticketService.viewTicketsAsReporter(user.getUsername(),
                    input.getTimestamp());
        }

        ObjectMapper mapper = InputReader.getMapper();
        ObjectNode out = mapper.createObjectNode();
        out.put("command", input.getCommand());
        out.put("username", input.getUsername());
        out.put("timestamp", input.getTimestamp());

        ArrayNode arr = out.putArray("tickets");
        for (Ticket t : tickets) {
            arr.add(mapper.valueToTree(t));
        }

        return CommandResult.success(out);
    }
}
