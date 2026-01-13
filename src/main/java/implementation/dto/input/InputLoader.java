package implementation.dto.input;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class InputLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public static List<CommandInput> loadCommands(String filePath) throws IOException {
        CommandInput[] commands = MAPPER.readValue(new File(filePath), CommandInput[].class);
        return Arrays.asList(commands);
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
