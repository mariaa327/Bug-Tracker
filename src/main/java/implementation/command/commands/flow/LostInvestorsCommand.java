package implementation.command.commands.flow;

import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.flow.ProjectContext;
import implementation.domain.user.Role;
import implementation.dto.input.CommandInput;
import implementation.repository.impl.JsonUserRepository;

public class LostInvestorsCommand implements Command{
    private final JsonUserRepository userRepo;
    private final ProjectContext ctx;

    public LostInvestorsCommand(JsonUserRepository userRepo, ProjectContext ctx) {
        this.userRepo = userRepo;
        this.ctx = ctx;
    }

    @Override
    public CommandResult execute(CommandInput input) {
        var userOpt = userRepo.findByUsername(input.getUsername());
        if (userOpt.isEmpty() || userOpt.get().getRole() != Role.MANAGER) {
            return null;
        }

        ctx.setInvestorsLost(true);
        return null;
    }
}
