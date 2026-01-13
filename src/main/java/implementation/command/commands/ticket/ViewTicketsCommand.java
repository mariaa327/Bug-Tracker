package implementation.command.commands.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.ticket.Ticket;
import implementation.domain.user.Role;
import implementation.domain.user.User;
import implementation.dto.input.CommandInput;
import implementation.dto.input.InputLoader;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.TicketService;

import java.util.List;

public class ViewTicketsCommand implements Command {
    private final JsonUserRepository userRepo;
    private final TicketService ticketService;

    public ViewTicketsCommand(JsonUserRepository userRepo, TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    @Override
    public CommandResult execute(CommandInput input) {
        User user = userRepo.findByUsername(input.getUsername()).orElse(null);
        if (user == null) {
            return CommandResult.error(input, "The user " + input.getUsername() + " does not exist.");
        }

        List<Ticket> tickets = switch (user.getRole()) {
            case MANAGER -> ticketService.viewTicketsAsManager(input.getTimestamp());
            case DEVELOPER -> ticketService.viewTicketsAsDeveloper(user.getUsername(), input.getTimestamp());
            case REPORTER -> ticketService.viewTicketsAsReporter(user.getUsername(), input.getTimestamp());
        };

        ObjectMapper mapper = InputLoader.getMapper();
        ObjectNode out = mapper.createObjectNode();
        out.put("command", input.getCommand());
        out.put("username", input.getUsername());
        out.put("timestamp", input.getTimestamp());

        ArrayNode arr = out.putArray("tickets");
        for (Ticket t : tickets)
            arr.add(mapper.valueToTree(t));

        return CommandResult.success(out);
    }
}
