package implementation.command.commands.flow;

import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.flow.ProjectContext;
import implementation.domain.user.Role;
import implementation.domain.user.User;
import implementation.dto.input.CommandInput;
import implementation.repository.impl.JsonUserRepository;

public class LostInvestorsCommand implements Command {
    private final JsonUserRepository userRepo;
    private final ProjectContext ctx;

    public LostInvestorsCommand(JsonUserRepository userRepo, ProjectContext ctx) {
        this.userRepo = userRepo;
        this.ctx = ctx;
    }

    @Override
    public CommandResult execute(CommandInput input) {
        User u = userRepo.findByUsername(input.getUsername()).orElse(null);
        if (u == null) {
            return CommandResult.error(input, "The user " + input.getUsername() + " does not exist.");
        }
        if (u.getRole() != Role.MANAGER) {
            return CommandResult.error(input,
                    "The user does not have permission to execute this command: required role MANAGER; user role "
                            + u.getRole() + ".");
        }

        ctx.setInvestorsLost(true);
        return null;
    }
}
