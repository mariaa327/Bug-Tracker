package implementation.command.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.input.CommandInput;
import implementation.input.InputReader;
import implementation.registry.UserRegistry;
import implementation.service.NotificationService;
import implementation.domain.user.User;
import java.util.List;

public class ViewNotificationsCommand implements Command {
    private final UserRegistry userRepo;
    private final NotificationService notificationService;

    /**
     * creates the view notifications command
     * @param userRepo the user repository
     * @param notificationService the notification service
     */
    public ViewNotificationsCommand(final UserRegistry userRepo,
                                    final NotificationService notificationService) {
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    /**
     * executes the view notifications command
     * the requesting user must exist
     * @param input the command input
     * @return a command result
     */
    @Override
    public CommandResult execute(final CommandInput input) {
        final User user = userRepo.findByUsername(input.getUsername());
        if (user == null) {
            return new CommandResult(errorNode(input,
                    "The user " + input.getUsername() + " does not exist."));
        }

        List<String> notifications = notificationService.viewAndClear(
                input.getUsername(),
                input.getTimestamp()
        );

        final ObjectMapper mapper = InputReader.getMapper();
        final ObjectNode node = mapper.createObjectNode();
        node.put("command", input.getCommand());
        node.put("username", input.getUsername());
        node.put("timestamp", input.getTimestamp());

        ArrayNode arr = mapper.createArrayNode();
        if (notifications != null) {
            for (int i = 0; i < notifications.size(); i++) {
                arr.add(notifications.get(i));
            }
        }
        node.set("notifications", arr);

        return CommandResult.success(node);
    }

    /**
     * builds the error node
     * @param input the command input
     * @param message the error message
     * @return the error node
     */
    private ObjectNode errorNode(final CommandInput input,
                                 final String message) {
        ObjectMapper mapper = InputReader.getMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("command", input.getCommand());
        node.put("username", input.getUsername());
        node.put("timestamp", input.getTimestamp());
        node.put("error", message);
        return node;
    }
}
