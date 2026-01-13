package implementation.command.commands.development;

import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.user.Role;
import implementation.domain.user.User;
import implementation.dto.input.CommandInput;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.TicketService;

public class UndoAssignTicketCommand implements Command {
    private final JsonUserRepository userRepo;
    private final TicketService ticketService;

    public UndoAssignTicketCommand(JsonUserRepository userRepo, TicketService ticketService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
    }

    @Override
    public CommandResult execute(CommandInput input) {
        User u = userRepo.findByUsername(input.getUsername()).orElse(null);
        if (u == null) return CommandResult.error(input, "The user " + input.getUsername() + " does not exist.");
        if (u.getRole() != Role.DEVELOPER) {
            return CommandResult.error(input,
                    "The user does not have permission to execute this command: required role DEVELOPER; user role "
                            + u.getRole() + ".");
        }

        Integer id = input.getTicketID();
        if (id == null)
            return null;

        String err = ticketService.undoAssignTicket(input.getUsername(), id, input.getTimestamp());
        if (err != null)
            return CommandResult.error(input, err);

        return null;
    }
}
