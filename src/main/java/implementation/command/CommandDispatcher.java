package implementation.command;

import implementation.command.commands.AddCommentCommand;
import implementation.command.commands.AppStabilityReportCommand;
import implementation.command.commands.AssignTicketCommand;
import implementation.command.commands.ChangeStatusCommand;
import implementation.command.commands.CreateMilestoneCommand;
import implementation.command.commands.GenerateCustomerImpactReportCommand;
import implementation.command.commands.GeneratePerformanceReportCommand;
import implementation.command.commands.GenerateResolutionEfficiencyReportCommand;
import implementation.command.commands.GenerateTicketRiskReportCommand;
import implementation.command.commands.LostInvestorsCommand;
import implementation.command.commands.ReportTicketCommand;
import implementation.command.commands.SearchCommand;
import implementation.command.commands.UndoAddCommentCommand;
import implementation.command.commands.UndoAssignTicketCommand;
import implementation.command.commands.UndoChangeStatusCommand;
import implementation.command.commands.ViewAssignedTicketsCommand;
import implementation.command.commands.ViewMilestonesCommand;
import implementation.command.commands.ViewNotificationsCommand;
import implementation.command.commands.ViewTicketHistoryCommand;
import implementation.command.commands.ViewTicketsCommand;
import implementation.domain.flow.ProjectContext;
import implementation.input.CommandInput;
import implementation.registry.UserRegistry;
import implementation.service.MilestoneService;
import implementation.service.NotificationService;
import implementation.service.TicketService;

public final class CommandDispatcher {
    private final UserRegistry userRepo;
    private final TicketService ticketService;
    private final MilestoneService milestoneService;
    private final ProjectContext ctx;
    private final NotificationService notificationService;

    public CommandDispatcher(final UserRegistry userRepo,
                             final TicketService ticketService,
                             final MilestoneService milestoneService,
                             final ProjectContext ctx,
                             final NotificationService notificationService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
        this.milestoneService = milestoneService;
        this.ctx = ctx;
        this.notificationService = notificationService;
    }

    /**
     * dispatches a command input to the corresponding command implementation
     *
     * @param input the command input to dispatch
     * @return the command result, or null if the input/command is
     * invalid or unknown
     */
    public CommandResult dispatch(final CommandInput input) {
        if (input == null || input.getCommand() == null) {
            return null;
        }

        return switch (input.getCommand()) {
            case "reportTicket" ->
                    new ReportTicketCommand(userRepo, ticketService).execute(input);
            case "viewTickets" ->
                    new ViewTicketsCommand(userRepo, ticketService).execute(input);

            case "createMilestone" ->
                    new CreateMilestoneCommand(
                            userRepo, milestoneService, notificationService).execute(input);
            case "viewMilestones" ->
                    new ViewMilestonesCommand(userRepo, milestoneService).execute(input);

            case "lostInvestors" ->
                    new LostInvestorsCommand(userRepo, ctx).execute(input);

            case "assignTicket" ->
                    new AssignTicketCommand(userRepo, ticketService).execute(input);
            case "undoAssignTicket" ->
                    new UndoAssignTicketCommand(userRepo, ticketService).execute(input);
            case "viewAssignedTickets" ->
                    new ViewAssignedTicketsCommand(userRepo, ticketService).execute(input);

            case "addComment" ->
                    new AddCommentCommand(userRepo, ticketService).execute(input);
            case "undoAddComment" ->
                    new UndoAddCommentCommand(userRepo, ticketService).execute(input);

            case "changeStatus" ->
                    new ChangeStatusCommand(userRepo, ticketService).execute(input);
            case "undoChangeStatus" ->
                    new UndoChangeStatusCommand(userRepo, ticketService).execute(input);
            case "viewTicketHistory" ->
                    new ViewTicketHistoryCommand(userRepo, ticketService).execute(input);

            case "search" ->
                    new SearchCommand(userRepo, ticketService, milestoneService).execute(input);
            case "viewNotifications" ->
                    new ViewNotificationsCommand(userRepo, notificationService).execute(input);

            case "generateCustomerImpactReport" ->
                    new GenerateCustomerImpactReportCommand(
                            userRepo, ticketService).execute(input);
            case "generateTicketRiskReport" ->
                    new GenerateTicketRiskReportCommand(
                            userRepo, ticketService).execute(input);
            case "generateResolutionEfficiencyReport" ->
                    new GenerateResolutionEfficiencyReportCommand(
                            userRepo, ticketService).execute(input);
            case "appStabilityReport" ->
                    new AppStabilityReportCommand(
                            userRepo, ticketService, ctx).execute(input);
            case "generatePerformanceReport" ->
                    new GeneratePerformanceReportCommand(
                            userRepo, ticketService).execute(input);

            default -> null;
        };
    }
}
