package implementation.command.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.input.CommandInput;
import implementation.input.InputReader;
import implementation.registry.UserRegistry;
import implementation.service.MilestoneService;
import java.util.List;
import java.util.Map;

public class ViewMilestonesCommand implements Command {
    private final UserRegistry userRepo;
    private final MilestoneService milestoneService;

    /**
     * creates the view milestones command
     * @param userRepo the user repository
     * @param milestoneService the milestone service
     */
    public ViewMilestonesCommand(final UserRegistry userRepo,
                                 final MilestoneService milestoneService) {
        this.userRepo = userRepo;
        this.milestoneService = milestoneService;
    }

    /**
     * executes the view milestones command
     * the requesting user must exist
     * @param input the command input
     * @return an error result if validation fails success result otherwise
     */
    @Override
    public CommandResult execute(final CommandInput input) {
        if (userRepo.findByUsername(input.getUsername()) == null) {
            return CommandResult.error(input,
                    "The user " + input.getUsername() + " does not exist.");
        }

        List<Map<String, Object>> milestoneData = milestoneService.viewMilestones(
                input.getUsername(),
                input.getTimestamp());

        ObjectMapper mapper = InputReader.getMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("command", input.getCommand());
        node.put("username", input.getUsername());
        node.put("timestamp", input.getTimestamp());
        node.set("milestones", mapper.valueToTree(milestoneData));

        return CommandResult.success(node);
    }
}
