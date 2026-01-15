package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import implementation.command.CommandDispatcher;
import implementation.command.CommandResult;
import implementation.domain.flow.ProjectContext;
import implementation.dto.input.CommandInput;
import implementation.dto.input.InputLoader;
import implementation.repository.impl.JsonUserRepository;
import implementation.repository.impl.InMemoryTicketRepository;
import implementation.service.TicketService;
import implementation.service.FlowService;
import implementation.repository.impl.InMemoryMilestoneRepository;
import implementation.service.MilestoneService;
import implementation.service.NotificationService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * main.App represents the main application logic that processes input commands,
 * generates outputs, and writes them to a file
 */
public class App {
    private App() {
    }

    private static final String INPUT_USERS_FIELD = "input/database/users.json";

    private static final ObjectWriter WRITER =
            new ObjectMapper().writer().withDefaultPrettyPrinter();

    /**
     * Runs the application: reads commands from an input file,
     * processes them, generates results, and writes them to an output file
     *
     * @param inputPath path to the input file containing commands
     * @param outputPath path to the file where results should be written
     */
    public static void run(final String inputPath, final String outputPath) {
        // feel free to change this if needed
        // however keep 'outputs' variable name to be used for writing
        List<ObjectNode> outputs = new ArrayList<>();

        /*
            TODO 1 :
            Load initial user data and commands. we strongly recommend using jackson library.
            you can use the reading from hw1 as a reference.
            however you can use some of the more advanced features of
            jackson library, available here: https://www.baeldung.com/jackson-annotations
        */

        try {
            List<CommandInput> commands = InputLoader.loadCommands(inputPath);

            ObjectMapper mapper = InputLoader.getMapper();
            JsonUserRepository userRepo = new JsonUserRepository(INPUT_USERS_FIELD, mapper);

            InMemoryTicketRepository ticketRepo = new InMemoryTicketRepository();
            InMemoryMilestoneRepository milestoneRepo = new InMemoryMilestoneRepository();
            NotificationService notificationService =
                    new NotificationService(milestoneRepo, ticketRepo);

            ProjectContext ctx = new ProjectContext();

            if (!commands.isEmpty()) {
                String minTs = commands.stream()
                        .map(CommandInput::getTimestamp)
                        .min(String::compareTo)
                        .orElse(commands.get(0).getTimestamp());

                ctx.setStartDateIfNull(minTs);
            }
            ctx.setInvestorsLost(false);

            FlowService flowService = new FlowService(ctx);
            TicketService ticketService = new TicketService(ticketRepo, flowService, userRepo, milestoneRepo);
            MilestoneService milestoneService = new MilestoneService(milestoneRepo, ticketRepo);

            CommandDispatcher dispatcher = new CommandDispatcher(userRepo,
                    ticketService, milestoneService, ctx, notificationService);


            /*
                TODO 2: process commands.
            */
            for (CommandInput input : commands) {
                if (ctx.areInvestorsLost()) {
                    break;
                }

                CommandResult result = dispatcher.dispatch(input);

                /*
                    TODO 3: create objectnodes for output
                */
                if (result != null && result.getOutput() != null) {
                    outputs.add(result.getOutput());
                }
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }

        // DO NOT CHANGE THIS SECTION IN ANY WAY
        try {
            File outputFile = new File(outputPath);
            outputFile.getParentFile().mkdirs();
            WRITER.withDefaultPrettyPrinter().writeValue(outputFile, outputs);
        } catch (IOException e) {
            System.out.println("error writing to output file: " + e.getMessage());
        }
    }
}
