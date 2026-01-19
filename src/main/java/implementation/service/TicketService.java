package implementation.service;

import implementation.domain.ticket.Ticket;
import implementation.input.ParamsReportTicket;
import implementation.registry.MilestoneRegistry;
import implementation.registry.TicketRegistry;
import implementation.registry.UserRegistry;
import implementation.service.helpers.*;

import java.util.List;
import java.util.Map;

public final class TicketService {
    private final TicketRegistry repo;
    private final FlowService flowService;
    private final UserRegistry userRepo;
    private final MilestoneRegistry milestoneRepo;

    private final TicketSearchService queryService;
    private final TicketCommandService commandService;
    private final PerformanceScoreCalculator performanceCalculator;

    /**
     * creates the ticket service facade
     * @param repo the ticket registry
     * @param flowService the flow service
     * @param userRepo the user registry
     * @param milestoneRepo the milestone registry
     */
    public TicketService(final TicketRegistry repo,
                         final FlowService flowService,
                         final UserRegistry userRepo,
                         final MilestoneRegistry milestoneRepo) {
        this.repo = repo;
        this.flowService = flowService;
        this.userRepo = userRepo;
        this.milestoneRepo = milestoneRepo;

        MilestonePriorityPolicy escalation =
                new MilestonePriorityPolicy(flowService, milestoneRepo);
        DeveloperAssignmentValidator validator = new DeveloperAssignmentValidator();

        this.queryService = new TicketSearchService(repo, milestoneRepo, escalation);
        this.commandService = new TicketCommandService(repo, userRepo,
                milestoneRepo, flowService, validator);
        this.performanceCalculator = new PerformanceScoreCalculator(repo, userRepo);
    }

    /**
     * views all tickets as manager
     * @param currentTimestamp the current timestamp
     * @return the list of visible tickets
     */
    public List<Ticket> viewTicketsAsManager(final String currentTimestamp) {
        return queryService.viewTicketsAsManager(currentTimestamp);
    }

    /**
     * views tickets reported by a given reporter
     * @param username the reporter username
     * @param currentTimestamp the current timestamp
     * @return the list of tickets reported by the user
     */
    public List<Ticket> viewTicketsAsReporter(final String username,
                                              final String currentTimestamp) {
        return queryService.viewTicketsAsReporter(username, currentTimestamp);
    }

    /**
     * views tickets available for a developer to pick up
     * @param devUsername the developer username
     * @param currentTimestamp the current timestamp
     * @return the list of assignable tickets
     */
    public List<Ticket> viewTicketsAsDeveloper(final String devUsername,
                                               final String currentTimestamp) {
        return queryService.viewTicketsAsDeveloper(devUsername, currentTimestamp);
    }

    /**
     * views tickets assigned to a developer
     * @param devUsername the developer username
     * @param timestamp the current timestamp
     * @return the list of assigned tickets sorted by priority
     */
    public List<Ticket> viewAssignedTickets(final String devUsername, final String timestamp) {
        return queryService.viewAssignedTickets(devUsername, timestamp);
    }

    /**
     * views ticket history for a developer
     * @param devUsername the developer username
     * @return tickets ever assigned to the developer
     */
    public List<Ticket> viewTicketHistoryTickets(final String devUsername) {
        return queryService.viewTicketHistoryTickets(devUsername);
    }

    /**
     * reports a new ticket
     * @param timestamp the command timestamp
     * @param params the report ticket params
     * @return null if ok an error message otherwise
     */
    public String reportTicket(final String timestamp, final ParamsReportTicket params) {
        return commandService.reportTicket(timestamp, params);
    }

    /**
     * assigns a ticket to a developer
     * @param devUsername the developer username
     * @param timestamp the command timestamp
     * @param ticketId the ticket id
     * @return null if ok an error message otherwise
     */
    public String assignTicket(final String devUsername,
                               final String timestamp,
                               final int ticketId) {
        return commandService.assignTicket(devUsername, timestamp, ticketId);
    }

    /**
     * undoes assignment for a ticket
     * @param devUsername the developer username
     * @param ticketId the ticket id
     * @param timestamp the command timestamp
     * @return null if ok an error message otherwise
     */
    public String undoAssignTicket(final String devUsername,
                                   final int ticketId,
                                   final String timestamp) {
        return commandService.undoAssignTicket(devUsername, ticketId, timestamp);
    }

    /**
     * adds a comment to a ticket
     * @param username the requesting username
     * @param ticketId the ticket id
     * @param comment the comment
     * @param timestamp the command timestamp
     * @return null if ok an error message otherwise
     */
    public String addComment(final String username,
                             final int ticketId,
                             final String comment,
                             final String timestamp) {
        return commandService.addComment(username, ticketId, comment, timestamp);
    }

    /**
     * undoes the last comment by a user
     * @param username the requesting username
     * @param ticketId the ticket id
     * @param timestamp the command timestamp
     * @return null if ok an error message otherwise
     */
    public String undoAddComment(final String username,
                                 final int ticketId,
                                 final String timestamp) {
        return commandService.undoAddComment(username, ticketId, timestamp);
    }

    /**
     * advances ticket status to the next status
     * @param devUsername the developer username
     * @param timestamp the command timestamp
     * @param ticketId the ticket id
     * @return null if ok an error message otherwise
     */
    public String changeStatus(final String devUsername,
                               final String timestamp,
                               final int ticketId) {
        return commandService.changeStatus(devUsername, timestamp, ticketId);
    }

    /**
     * reverts ticket status to the previous status
     * @param devUsername the developer username
     * @param timestamp the command timestamp
     * @param ticketId the ticket id
     * @return null if ok an error message otherwise
     */
    public String undoChangeStatus(final String devUsername,
                                   final String timestamp,
                                   final int ticketId) {
        return commandService.undoChangeStatus(devUsername, timestamp, ticketId);
    }

    /**
     * generates the performance report for the manager
     * @param managerUsername the manager username
     * @param timestamp the command timestamp
     * @return a list of rows for the report
     */
    public List<Map<String, Object>> generatePerformanceReport(final String managerUsername,
                                                               final String timestamp) {
        return performanceCalculator.generate(managerUsername, timestamp);
    }

    /**
     * returns all tickets
     * @return all tickets
     */
    public List<Ticket> getAllTickets() {
        return repo.getAll();
    }
}
