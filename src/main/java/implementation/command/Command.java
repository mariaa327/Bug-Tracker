package implementation.command;

import implementation.dto.input.CommandInput;

public interface Command {
    CommandResult execute(CommandInput input);
}