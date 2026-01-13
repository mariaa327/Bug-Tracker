package implementation.command.commands.milestone;

import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.user.Role;
import implementation.domain.user.User;
import implementation.dto.input.CommandInput;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.MilestoneService;

public class CreateMilestoneCommand implements Command {
    private final JsonUserRepository userRepo;
    private final MilestoneService milestoneService;

    public CreateMilestoneCommand(JsonUserRepository userRepo, MilestoneService milestoneService) {
        this.userRepo = userRepo;
        this.milestoneService = milestoneService;
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

        String err = milestoneService.createMilestone(input);
        if (err != null)
            return CommandResult.error(input, err);

        return null;
    }
}
