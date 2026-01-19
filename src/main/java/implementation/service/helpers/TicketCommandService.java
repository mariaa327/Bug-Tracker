package implementation.service.helpers;

import implementation.domain.milestone.Milestone;
import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.Ticket;
import implementation.domain.ticket.TicketStatus;
import implementation.domain.ticket.TicketType;
import implementation.domain.user.Role;
import implementation.domain.user.User;
import implementation.factory.TicketFactory;
import implementation.input.ParamsReportTicket;
import implementation.registry.MilestoneRegistry;
import implementation.registry.TicketRegistry;
import implementation.registry.UserRegistry;
import implementation.service.FlowService;

import java.time.LocalDate;

/**
 * helper service that handles ticket-changing operations such as report, assign,
 * status changes and comment actions, including all validation rules
 */
public final class TicketCommandService {
    private static final int MIN_COMMENT_LENGTH = 10;

    private final TicketRegistry repo;
    private final UserRegistry userRepo;
    private final MilestoneRegistry milestoneRepo;
    private final FlowService flowService;
    private final DeveloperAssignmentValidator validator;

    /**
     * creates the ticket command service
     * @param repo the ticket registry
     * @param userRepo the user registry
     * @param milestoneRepo the milestone registry
     * @param flowService the flow service
     * @param validator the eligibility validator
     */
    public TicketCommandService(final TicketRegistry repo,
                                final UserRegistry userRepo,
                                final MilestoneRegistry milestoneRepo,
                                final FlowService flowService,
                                final DeveloperAssignmentValidator validator) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.milestoneRepo = milestoneRepo;
        this.flowService = flowService;
        this.validator = validator;
    }

    /**
     * reports a new ticket
     * @param timestamp the command timestamp
     * @param params the report params
     * @return null if ok an error message otherwise
     */
    public String reportTicket(final String timestamp, final ParamsReportTicket params) {
        String phaseError = flowService.validateTestingPhase(timestamp);
        if (phaseError != null) {
            return phaseError;
        }

        TicketType type = TicketType.valueOf(params.getType());

        String reportedBy = params.getReportedBy();
        boolean anonymous = (reportedBy == null || reportedBy.isEmpty());

        if (anonymous) {
            if (type == TicketType.BUG) {
                params.setBusinessPriority(BusinessPriority.LOW.name());
            } else {
                return "Anonymous reports are only allowed for tickets of type BUG.";
            }
        }

        Ticket ticket = TicketFactory.createTicket(timestamp, params);
        ticket.setId(repo.nextId());
        repo.add(ticket);

        return null;
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
        Ticket t = repo.findById(ticketId);
        if (t == null) {
            return "Ticket not found.";
        }

        Milestone m = milestoneRepo.findByTicketId(ticketId);
        if (m == null) {
            return "Ticket not found.";
        }

        String milestoneName = m.getName();

        if (milestoneRepo.isMilestoneBlocked(milestoneName)) {
            return "Cannot assign ticket " + ticketId
                    + " from blocked milestone " + milestoneName + ".";
        }

        if (!milestoneRepo.isDevAssignedToTicketMilestone(ticketId, devUsername)) {
            return "Developer " + devUsername
                    + " is not assigned to milestone " + milestoneName + ".";
        }

        if (t.getStatus() != TicketStatus.OPEN) {
            return "Only OPEN tickets can be assigned.";
        }

        User dev = userRepo.findByUsername(devUsername);
        if (dev == null) {
            return "The user " + devUsername + " does not exist.";
        }

        String err = validator.validateDevCanAssign(dev, t, ticketId, devUsername);
        if (err != null) {
            return err;
        }

        TicketStatus before = t.getStatus();

        t.assignTo(devUsername, LocalDate.parse(timestamp));
        t.logAssigned(devUsername, timestamp);
        t.logStatusChanged(before, t.getStatus(), devUsername, timestamp);

        return null;
    }

    /**
     * undoes a ticket assignment
     * @param devUsername the developer username
     * @param ticketId the ticket id
     * @param timestamp the command timestamp
     * @return null if ok an error message otherwise
     */
    public String undoAssignTicket(final String devUsername,
                                   final int ticketId,
                                   final String timestamp) {
        Ticket t = repo.findById(ticketId);
        if (t == null) {
            return "Ticket not found.";
        }

        if (t.getAssignedTo() == null || t.getAssignedTo().isEmpty()) {
            return "Cannot undo assignment.";
        }

        if (!devUsername.equals(t.getAssignedTo())) {
            return "Cannot undo assignment.";
        }

        t.unassign();
        t.setStatus(TicketStatus.OPEN);
        t.logDeAssigned(devUsername, timestamp);

        return null;
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
        Ticket t = repo.findById(ticketId);
        if (t == null) {
            return null;
        }

        if (t.isAnonymous()) {
            return "Comments are not allowed on anonymous tickets.";
        }

        if (comment == null || comment.length() < MIN_COMMENT_LENGTH) {
            return "Comment must be at least "
                    + MIN_COMMENT_LENGTH + " characters long.";
        }

        User u = userRepo.findByUsername(username);
        if (u == null) {
            return "User not found.";
        }

        Role role = u.getRole();
        if (role != Role.REPORTER && role != Role.DEVELOPER) {
            return "User not allowed.";
        }

        if (role == Role.REPORTER && t.getStatus() == TicketStatus.CLOSED) {
            return "Reporters cannot comment on CLOSED tickets.";
        }

        if (role == Role.DEVELOPER && !username.equals(t.getAssignedTo())) {
            return "Ticket " + ticketId
                    + " is not assigned to the developer " + username + ".";
        }

        if (role == Role.REPORTER && !username.equals(t.getReportedBy())) {
            return "Reporter " + username
                    + " cannot comment on ticket " + ticketId + ".";
        }

        t.addComment(username, comment, timestamp);
        return null;
    }

    /**
     * undoes the last comment by a user
     * @param username the username
     * @param ticketId the ticket id
     * @param timestamp the command timestamp
     * @return null if ok an error message otherwise
     */
    public String undoAddComment(final String username,
                                 final int ticketId,
                                 final String timestamp) {
        Ticket t = repo.findById(ticketId);
        if (t == null) {
            return null;
        }

        if (t.isAnonymous()) {
            return "Comments are not allowed on anonymous tickets.";
        }

        t.undoLastCommentBy(username);
        return null;
    }

    /**
     * advances the ticket status
     * @param devUsername the developer username
     * @param timestamp the command timestamp
     * @param ticketId the ticket id
     * @return null if ok an error message otherwise
     */
    public String changeStatus(final String devUsername,
                               final String timestamp,
                               final int ticketId) {
        Ticket t = repo.findById(ticketId);
        if (t == null) {
            return "Ticket not found.";
        }

        if (t.getAssignedTo() == null || t.getAssignedTo().isEmpty()) {
            return "Ticket " + ticketId
                    + " is not assigned to developer " + devUsername + ".";
        }

        if (!devUsername.equals(t.getAssignedTo())) {
            return "Ticket " + ticketId
                    + " is not assigned to developer " + devUsername + ".";
        }

        TicketStatus from = t.getStatus();
        TicketStatus to = from.next();

        if (from == to) {
            return null;
        }

        t.setStatus(to);
        t.logStatusChanged(from, to, devUsername, timestamp);

        LocalDate now = LocalDate.parse(timestamp);

        if (to == TicketStatus.RESOLVED || to == TicketStatus.CLOSED) {
            t.setLastResolvedOrClosedAt(now);
        }

        if (to == TicketStatus.RESOLVED) {
            t.setSolvedAtDate(now);
        }

        if (to == TicketStatus.CLOSED) {
            t.setClosedAt(now);
            if (t.getSolvedAtDate() == null) {
                t.setSolvedAtDate(now);
            }
        }

        return null;
    }

    /**
     * reverts the ticket status
     * @param devUsername the developer username
     * @param timestamp the command timestamp
     * @param ticketId the ticket id
     * @return null if ok an error message otherwise
     */
    public String undoChangeStatus(final String devUsername,
                                   final String timestamp,
                                   final int ticketId) {
        Ticket t = repo.findById(ticketId);
        if (t == null) {
            return "Ticket not found.";
        }

        if (t.getAssignedTo() == null || t.getAssignedTo().isEmpty()) {
            return "Ticket " + ticketId
                    + " is not assigned to developer " + devUsername + ".";
        }

        if (!devUsername.equals(t.getAssignedTo())) {
            return "Ticket " + ticketId
                    + " is not assigned to developer " + devUsername + ".";
        }

        TicketStatus from = t.getStatus();
        TicketStatus to = from.prev();

        if (from != to) {
            t.setStatus(to);
            t.logStatusChanged(from, to, devUsername, timestamp);
        }

        if (from == TicketStatus.CLOSED && to != TicketStatus.CLOSED) {
            t.setClosedAt(null);
        }

        return null;
    }
}
