package implementation.command.commands;

import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.user.Role;
import implementation.domain.user.User;
import implementation.input.CommandInput;
import implementation.registry.UserRegistry;
import implementation.service.MilestoneService;
import implementation.service.NotificationService;

import java.time.LocalDate;

public final class CreateMilestoneCommand implements Command {
    private final UserRegistry userRepo;
    private final MilestoneService milestoneService;
    private final NotificationService notificationService;

    /**
     * creates a command used to create milestones and notify assigned
     * developers
     * @param userRepo the user repository used for user validation
     * @param milestoneService the milestone service used to create milestones
     * @param notificationService the notification service used to notify developers
     */
    public CreateMilestoneCommand(final UserRegistry userRepo,
                                  final MilestoneService milestoneService,
                                  final NotificationService notificationService) {
        this.userRepo = userRepo;
        this.milestoneService = milestoneService;
        this.notificationService = notificationService;
    }

    /**
     * executes the create milestone command
     * the requesting user must exist and must have the manager role
     * @param input the command input
     * @return an error result if validation fails null otherwise
     */
    @Override
    public CommandResult execute(final CommandInput input) {
        final User u = userRepo.findByUsername(input.getUsername());
        if (u == null) {
            return CommandResult.error(
                    input,
                    "The user " + input.getUsername() + " does not exist.");
        }

        if (u.getRole() != Role.MANAGER) {
            return CommandResult.error(
                    input,
                    "The user does not have permission to execute this command: "
                            + "required role MANAGER; user role " + u.getRole() + ".");
        }

        String err = milestoneService.createMilestone(input);
        if (err != null) {
            return CommandResult.error(input, err);
        }

        notificationService.notifyNewMilestone(
                input.getAssignedDevs(),
                input.getName(),
                LocalDate.parse(input.getDueDate()));

        return null;
    }
}
