package implementation.command.commands;

import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.flow.ProjectContext;
import implementation.domain.user.Role;
import implementation.input.CommandInput;
import implementation.registry.UserRegistry;
import implementation.domain.user.User;

public final class LostInvestorsCommand implements Command {
    private final UserRegistry userRepo;
    private final ProjectContext ctx;

    /**
     * creates the lost investors command
     * @param userRepo the user repository
     * @param ctx the project context
     */
    public LostInvestorsCommand(final UserRegistry userRepo, final ProjectContext ctx) {
        this.userRepo = userRepo;
        this.ctx = ctx;
    }

    /**
     * executes the lost investors command
     * the requesting user must exist and must have the manager role
     * @param input the command input
     * @return null
     */
    @Override
    public CommandResult execute(final CommandInput input) {
        User user = userRepo.findByUsername(input.getUsername());
        if (user == null || user.getRole() != Role.MANAGER) {
            return null;
        }

        ctx.setInvestorsLost(true);
        return null;
    }
}
