package implementation.command;

import implementation.command.commands.development.AssignTicketCommand;
import implementation.command.commands.development.UndoAssignTicketCommand;
import implementation.command.commands.development.ViewAssignedTicketsCommand;
import implementation.command.commands.flow.LostInvestorsCommand;
import implementation.command.commands.milestone.CreateMilestoneCommand;
import implementation.command.commands.milestone.ViewMilestonesCommand;
import implementation.command.commands.ticket.ReportTicketCommand;
import implementation.command.commands.ticket.ViewTicketsCommand;
import implementation.domain.flow.ProjectContext;
import implementation.dto.input.CommandInput;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.MilestoneService;
import implementation.service.TicketService;

public class CommandDispatcher {
    private final JsonUserRepository userRepo;
    private final TicketService ticketService;
    private final MilestoneService milestoneService;
    private final ProjectContext ctx;

    public CommandDispatcher(JsonUserRepository userRepo,
                             TicketService ticketService,
                             MilestoneService milestoneService,
                             ProjectContext ctx) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
        this.milestoneService = milestoneService;
        this.ctx = ctx;
    }

    public CommandResult dispatch(CommandInput input) {
        return switch (input.getCommand()) {
            case "reportTicket" -> new ReportTicketCommand(userRepo, ticketService).execute(input);
            case "viewTickets" -> new ViewTicketsCommand(userRepo, ticketService).execute(input);
            case "createMilestone" -> new CreateMilestoneCommand(userRepo, milestoneService).execute(input);
            case "viewMilestones" -> new ViewMilestonesCommand(userRepo, milestoneService).execute(input);
            case "assignTicket" -> new AssignTicketCommand(userRepo, ticketService).execute(input);
            case "undoAssignTicket" -> new UndoAssignTicketCommand(userRepo, ticketService).execute(input);
            case "viewAssignedTickets" -> new ViewAssignedTicketsCommand(userRepo, ticketService).execute(input);
            case "lostInvestors" -> new LostInvestorsCommand(userRepo, ctx).execute(input);
            default -> null;
        };
    }
}
