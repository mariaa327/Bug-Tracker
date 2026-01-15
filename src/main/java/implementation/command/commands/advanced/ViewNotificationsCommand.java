package implementation.command.commands.advanced;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.dto.input.CommandInput;
import implementation.dto.input.InputLoader;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.NotificationService;

import java.util.List;

public class ViewNotificationsCommand implements Command {

    private final JsonUserRepository userRepo;
    private final NotificationService notificationService;

    public ViewNotificationsCommand(JsonUserRepository userRepo,
                                    NotificationService notificationService) {
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    @Override
    public CommandResult execute(CommandInput input) {
        if (userRepo.findByUsername(input.getUsername()).isEmpty()) {
            return new CommandResult(errorNode(input, "User not found."));
        }

        List<String> notes = notificationService.viewAndClear(input.getUsername(), input.getTimestamp());

        ObjectMapper mapper = InputLoader.getMapper();
        ObjectNode out = mapper.createObjectNode();
        out.put("command", input.getCommand());
        out.put("username", input.getUsername());
        out.put("timestamp", input.getTimestamp());

        ArrayNode arr = mapper.createArrayNode();
        for (String s : notes) arr.add(s);
        out.set("notifications", arr);

        return new CommandResult(out);
    }

    private ObjectNode errorNode(CommandInput input, String message) {
        ObjectMapper mapper = InputLoader.getMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("command", input.getCommand());
        node.put("username", input.getUsername());
        node.put("timestamp", input.getTimestamp());
        node.put("error", message);
        return node;
    }
}
