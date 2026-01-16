package implementation.command.commands.milestone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.dto.input.CommandInput;
import implementation.dto.input.InputLoader;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.MilestoneService;
import java.util.List;
import java.util.Map;

public class ViewMilestonesCommand implements Command {
    private final JsonUserRepository userRepo;
    private final MilestoneService milestoneService;

    public ViewMilestonesCommand(JsonUserRepository userRepo, MilestoneService milestoneService) {
        this.userRepo = userRepo;
        this.milestoneService = milestoneService;
    }

    @Override
    public CommandResult execute(CommandInput input) {
        if (!userRepo.findByUsername(input.getUsername()).isPresent()) {
            return CommandResult.error(input, "The user " + input.getUsername() + " does not exist.");
        }

        List<Map<String, Object>> milestoneData = milestoneService.viewMilestones(
                input.getUsername(),
                input.getTimestamp()
        );

        ObjectMapper mapper = InputLoader.getMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("command", input.getCommand());
        node.put("username", input.getUsername());
        node.put("timestamp", input.getTimestamp());
        node.set("milestones", mapper.valueToTree(milestoneData));

        return CommandResult.success(node);
    }
}
