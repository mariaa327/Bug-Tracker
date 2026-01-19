package implementation.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.input.CommandInput;
import implementation.input.InputReader;

public final class CommandResult {
    private final ObjectNode output;

    public CommandResult(final ObjectNode output) {
        this.output = output;
    }

    /**
     * returns the JSON output associated with this command result
     * @return the output node
     */
    public ObjectNode getOutput() {
        return output;
    }

    /**
     * builds an error with standard fields and an error message
     * @param input the command input that produced the error
     * @param message the error message
     * @return a command result containing the error payload
     */
    public static CommandResult error(final CommandInput input, final String message) {
        final ObjectMapper mapper = InputReader.getMapper();
        final ObjectNode node = mapper.createObjectNode();
        node.put("command", input.getCommand());
        node.put("username", input.getUsername());
        node.put("timestamp", input.getTimestamp());
        node.put("error", message);
        return new CommandResult(node);
    }

    /**
     * builds a successful commandResult
     * @param output the output payload
     * @return a command result containing the provided payload
     */
    public static CommandResult success(final ObjectNode output) {
        return new CommandResult(output);
    }
}
