package implementation.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.dto.input.CommandInput;
import implementation.dto.input.InputLoader;

public class CommandResult {
    private final ObjectNode output;

    public CommandResult(ObjectNode output) {
        this.output = output;
    }

    public ObjectNode getOutput() {
        return output;
    }

    public static CommandResult error(CommandInput input, String message) {
        ObjectMapper mapper = InputLoader.getMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("command", input.getCommand());
        node.put("username", input.getUsername());
        node.put("timestamp", input.getTimestamp());
        node.put("error", message);
        return new CommandResult(node);
    }

    public static CommandResult success(ObjectNode output) {
        return new CommandResult(output);
    }
}
