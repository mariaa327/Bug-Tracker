package implementation.command;

import implementation.command.commands.ticket.ReportTicketCommand;
import implementation.command.commands.ticket.ViewTicketsCommand;
import implementation.domain.flow.ProjectContext;
import implementation.dto.input.CommandInput;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.TicketService;
import implementation.command.commands.milestone.CreateMilestoneCommand;
import implementation.command.commands.milestone.ViewMilestonesCommand;
import implementation.command.commands.ticket.ReportTicketCommand;
import implementation.command.commands.ticket.ViewTicketsCommand;
import implementation.dto.input.CommandInput;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.MilestoneService;
import implementation.service.TicketService;
import implementation.command.commands.flow.LostInvestorsCommand;
import implementation.command.commands.development.AssignTicketCommand;
import implementation.command.commands.development.UndoAssignTicketCommand;
import implementation.command.commands.development.ViewAssignedTicketsCommand;
import implementation.command.commands.ticket.AddCommentCommand;
import implementation.command.commands.ticket.UndoAddCommentCommand;

public class CommandDispatcher {
    private final JsonUserRepository userRepo;
    private final TicketService ticketService;
    private final MilestoneService milestoneService;
    private final ProjectContext ctx;

    public CommandDispatcher(JsonUserRepository userRepo, TicketService ticketService,
                             MilestoneService milestoneService,
                             ProjectContext ctx) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
        this.milestoneService = milestoneService;
        this.ctx = ctx;
    }

    public CommandResult dispatch(CommandInput input) {
        if ("reportTicket".equals(input.getCommand())) {
            return new ReportTicketCommand(userRepo, ticketService).execute(input);
        } else if ("viewTickets".equals(input.getCommand())) {
            return new ViewTicketsCommand(userRepo, ticketService).execute(input);
        } else if ("createMilestone".equals(input.getCommand())) {
            return new CreateMilestoneCommand(userRepo, milestoneService).execute(input);
        } else if ("viewMilestones".equals(input.getCommand())) {
            return new ViewMilestonesCommand(userRepo, milestoneService).execute(input);
        } else if ("lostInvestors".equals(input.getCommand())) {
            return new LostInvestorsCommand(userRepo, ctx).execute(input);
        } else if ("assignTicket".equals(input.getCommand())) {
            return new AssignTicketCommand(userRepo, ticketService).execute(input);
        } else if ("undoAssignTicket".equals(input.getCommand())) {
            return new UndoAssignTicketCommand(userRepo, ticketService).execute(input);
        } else if ("viewAssignedTickets".equals(input.getCommand())) {
            return new ViewAssignedTicketsCommand(userRepo, ticketService).execute(input);
        } else if ("addComment".equals(input.getCommand())) {
            return new AddCommentCommand(userRepo, ticketService).execute(input);
        } else if ("undoAddComment".equals(input.getCommand())) {
            return new UndoAddCommentCommand(userRepo, ticketService).execute(input);
        }
        return null;
    }
}