package implementation.input;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * utility class for loading command inputs from a JSON file and for
 * providing a shared
 * ObjectMapper configured for the project
 */
public final class InputReader {
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * private constructor to prevent instantiation of this utility class
     */
    private InputReader() {
    }

    /**
     * loads the list of CommandInput commands from a JSON file path
     * @param filePath the path to the input JSON file
     * @return a list containing all parsed CommandInput objects
     * @throws IOException if the file cannot be read or parsed as JSON
     */
    public static List<CommandInput> loadCommands(final String filePath) throws IOException {
        CommandInput[] commands = MAPPER.readValue(new File(filePath), CommandInput[].class);

        List<CommandInput> list = new ArrayList<>();
        for (int i = 0; i < commands.length; i++) {
            list.add(commands[i]);
        }
        return list;
    }

    /**
     * returns the shared ObjectMapper instance used throughout the project
     * @return the shared configured ObjectMapper
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
