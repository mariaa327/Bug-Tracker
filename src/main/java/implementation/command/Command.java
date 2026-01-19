package implementation.command;

import implementation.input.CommandInput;

public interface Command {
    /**
     * executes a command using the provided input
     * @param input the command input
     * @return a command result
     */
    CommandResult execute(CommandInput input);
}