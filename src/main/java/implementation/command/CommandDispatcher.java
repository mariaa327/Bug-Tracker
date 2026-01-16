package implementation.command;

import implementation.command.commands.ticket.ReportTicketCommand;
import implementation.command.commands.ticket.ViewTicketsCommand;
import implementation.domain.flow.ProjectContext;
import implementation.dto.input.CommandInput;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.NotificationService;
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
import implementation.command.commands.development.ChangeStatusCommand;
import implementation.command.commands.development.ViewTicketHistoryCommand;
import implementation.command.commands.development.UndoChangeStatusCommand;
import implementation.command.commands.advanced.SearchCommand;
import implementation.service.NotificationService;
import implementation.command.commands.advanced.ViewNotificationsCommand;
import implementation.command.commands.reports.GenerateCustomerImpactReportCommand;
import implementation.command.commands.reports.GenerateTicketRiskReportCommand;
import implementation.command.commands.reports.GenerateResolutionEfficiencyReportCommand;
import implementation.command.commands.reports.AppStabilityReportCommand;
import implementation.command.commands.reports.GeneratePerformanceReportCommand;

public class CommandDispatcher {
    private final JsonUserRepository userRepo;
    private final TicketService ticketService;
    private final MilestoneService milestoneService;
    private final ProjectContext ctx;
    private final NotificationService notificationService;

    public CommandDispatcher(JsonUserRepository userRepo, TicketService ticketService,
                             MilestoneService milestoneService,
                             ProjectContext ctx, NotificationService notificationService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
        this.milestoneService = milestoneService;
        this.ctx = ctx;
        this.notificationService = notificationService;
    }

    public CommandResult dispatch(CommandInput input) {
        if ("reportTicket".equals(input.getCommand())) {
            return new ReportTicketCommand(userRepo, ticketService).execute(input);
        } else if ("viewTickets".equals(input.getCommand())) {
            return new ViewTicketsCommand(userRepo, ticketService).execute(input);
        } else if ("createMilestone".equals(input.getCommand())) {
            return new CreateMilestoneCommand(userRepo, milestoneService,
                    notificationService).execute(input);
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
        } else if ("changeStatus".equals(input.getCommand())) {
            return new ChangeStatusCommand(userRepo, ticketService).execute(input);
        } else if ("viewTicketHistory".equals(input.getCommand())) {
            return new ViewTicketHistoryCommand(userRepo, ticketService).execute(input);
        } else if ("undoChangeStatus".equals(input.getCommand())) {
            return new UndoChangeStatusCommand(userRepo, ticketService).execute(input);
        } else if ("search".equals(input.getCommand())) {
            return new SearchCommand(userRepo, ticketService, milestoneService).execute(input);
        } else if ("viewNotifications".equals(input.getCommand())) {
            return new ViewNotificationsCommand(userRepo, notificationService).execute(input);
        } else if ("generateCustomerImpactReport".equals(input.getCommand())) {
            return new GenerateCustomerImpactReportCommand(userRepo, ticketService).execute(input);
        } else if ("generateTicketRiskReport".equals(input.getCommand())) {
            return new GenerateTicketRiskReportCommand(userRepo, ticketService).execute(input);
        } else if ("generateResolutionEfficiencyReport".equals(input.getCommand())) {
            return new GenerateResolutionEfficiencyReportCommand(userRepo, ticketService).execute(input);
        } else if ("appStabilityReport".equals(input.getCommand())) {
            return new AppStabilityReportCommand(userRepo, ticketService, ctx).execute(input);
        } else if ("generatePerformanceReport".equals(input.getCommand())) {
            return new GeneratePerformanceReportCommand(userRepo, ticketService).execute(input);
        }
        return null;
    }
}
