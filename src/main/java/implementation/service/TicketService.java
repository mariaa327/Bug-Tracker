package implementation.service;

import implementation.domain.flow.ProjectContext;
import implementation.domain.milestone.Milestone;
import implementation.domain.ticket.*;
import implementation.repository.impl.InMemoryMilestoneRepository;
import implementation.repository.impl.InMemoryTicketRepository;
import implementation.repository.impl.JsonUserRepository;

import implementation.domain.user.ExpertiseArea;
import implementation.domain.user.SeniorityLevel;
import implementation.domain.user.User;
import implementation.domain.user.Role;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TicketService {
    private final InMemoryTicketRepository repo;
    private final FlowService flowService;
    private final JsonUserRepository userRepo;
    private final InMemoryMilestoneRepository milestoneRepo;
    private final Map<Integer, LocalDate> lastEscalatedOn = new HashMap<>();

    public TicketService(InMemoryTicketRepository repo, FlowService flowService, JsonUserRepository userRepo,
                         InMemoryMilestoneRepository milestoneRepo) {
        this.repo = repo;
        this.flowService = flowService;
        this.userRepo = userRepo;
        this.milestoneRepo = milestoneRepo;
    }

    public List<Ticket> viewTicketsAsManager(String currentTimestamp) {
        LocalDate now = LocalDate.parse(currentTimestamp);
        List<Ticket> tickets = repo.getAll();
        applyMilestoneEscalation(tickets, now);

        tickets.sort(Comparator.comparing(Ticket::getCreatedAt).thenComparingInt(Ticket::getId));
        return tickets;
    }

    public List<Ticket> viewTicketsAsReporter(String username, String currentTimestamp) {
        LocalDate now = LocalDate.parse(currentTimestamp);

        List<Ticket> tickets = repo.getAll().stream()
                .filter(t -> t.getReportedBy() != null && t.getReportedBy().equals(username))
                .collect(Collectors.toList());

        applyMilestoneEscalation(tickets, now);

        tickets.sort(Comparator.comparing(Ticket::getCreatedAt).thenComparingInt(Ticket::getId));

        return tickets;
    }

    private void applyMilestoneEscalation(List<Ticket> tickets, LocalDate now) {
        ProjectContext ctx = flowService.getContext();
        if (ctx.getStartDate() == null) {
            return;
        }

        for (Ticket t : tickets) {
            if (t.getStatus() != TicketStatus.OPEN) {
                continue;
            }

            Optional<Milestone> mOpt = milestoneRepo.findByTicketId(t.getId());
            if (mOpt.isEmpty()) {
                continue;
            }

            Milestone m = mOpt.get();
            if (milestoneRepo.isMilestoneBlocked(m.getName())) {
                continue;
            }

            if (now.equals(m.getDueDate().minusDays(1))) {
                if (t.getBusinessPriority() != BusinessPriority.CRITICAL) {
                    t.setBusinessPriority(BusinessPriority.CRITICAL);
                }
                lastEscalatedOn.put(t.getId(), now);
                continue;
            }

            LocalDate last = lastEscalatedOn.get(t.getId());
            if (last == null) {
                last = m.getCreatedAt();
            }

            LocalDate cursor = last.plusDays(1);
            while (!cursor.isAfter(now)) {
                long daysPassed = cursor.toEpochDay() - m.getCreatedAt().toEpochDay();
                if (daysPassed > 0 && daysPassed % 3 == 0) {
                    t.setBusinessPriority(t.getBusinessPriority().bump());
                }
                cursor = cursor.plusDays(1);
            }

            lastEscalatedOn.put(t.getId(), now);
        }
    }

    public String reportTicket(String timestamp, implementation.dto.input.ParamsReportTicket params) {
        String phaseError = flowService.validateTestingPhase(timestamp);
        if (phaseError != null) {
            return phaseError;
        }

        TicketType type = TicketType.valueOf(params.getType());
        BusinessPriority priority = BusinessPriority.valueOf(params.getBusinessPriority());

        String reportedBy = params.getReportedBy();

        if (reportedBy == null || reportedBy.isEmpty()) {
            if (type == TicketType.BUG) {
                priority = BusinessPriority.LOW;
            } else {
                return "Anonymous reports are only allowed for tickets of type BUG.";
            }
        }



        LocalDate creationDate = LocalDate.parse(timestamp);

        ExpertiseArea exp = ExpertiseArea.valueOf(params.getExpertiseArea());

        Ticket ticket = switch (type) {
            case BUG -> new BugTicket(params.getTitle(), priority, creationDate, reportedBy, exp, null);
            case FEATURE_REQUEST -> new FeatureRequestTicket(params.getTitle(), priority, creationDate, reportedBy, exp, null);
            case UI_FEEDBACK -> new UiFeedbackTicket(params.getTitle(), priority, creationDate, reportedBy, exp, null);
        };

        ticket.setId(repo.nextId());
        repo.add(ticket);
        return null;
    }

    public String assignTicket(String devUsername, String timestamp, int ticketId) {
        var opt = repo.findById(ticketId);
        if (opt.isEmpty()) {
            return "Ticket not found.";
        }

        Ticket t = opt.get();

        Optional<Milestone> mileOpt = milestoneRepo.findByTicketId(ticketId);
        if (mileOpt.isEmpty()) {
            return "Ticket not found.";
        }
        String milestoneName = mileOpt.get().getName();

        if (milestoneRepo.isMilestoneBlocked(milestoneName)) {
            return "Cannot assign ticket " + ticketId + " from blocked milestone " + milestoneName + ".";
        }

        if (!milestoneRepo.isDevAssignedToTicketMilestone(ticketId, devUsername)) {
            return "Developer " + devUsername + " is not assigned to milestone " + milestoneName + ".";
        }

        if (t.getStatus() != TicketStatus.OPEN) {
            return "Only OPEN tickets can be assigned.";
        }

        Optional<User> devOpt = userRepo.findByUsername(devUsername);
        if (devOpt.isEmpty()) {
            return "The user " + devUsername + " does not exist.";
        }
        User dev = devOpt.get();

        if (!canHandle(dev.getExpertiseArea(), t.getExpertiseArea())) {
            return "Developer " + devUsername + " cannot assign ticket " + ticketId
                    + " due to expertise area. Required: " + requiredExpertiseList(t.getExpertiseArea())
                    + "; Current: " + dev.getExpertiseArea() + ".";
        }

        String seniorityReq = requiredSeniorityList(t.getBusinessPriority());
        if (seniorityReq != null && !hasRequiredSeniority(dev.getSeniorityLevel(), t.getBusinessPriority())) {
            return "Developer " + devUsername + " cannot assign ticket " + ticketId
                    + " due to seniority level. Required: " + seniorityReq
                    + "; Current: " + dev.getSeniorityLevel() + ".";
        }

        TicketStatus before = t.getStatus();
        t.assignTo(devUsername, LocalDate.parse(timestamp));

        t.logAssigned(devUsername, timestamp);
        t.logStatusChanged(before, t.getStatus(), devUsername, timestamp);

        return null;
    }

    public List<Ticket> viewAssignedTickets(String devUsername,
                                            String timestamp) {
        LocalDate now = LocalDate.parse(timestamp);
        List<Ticket> assigned = repo.getAll().stream()
                .filter(t -> devUsername.equals(t.getAssignedTo()))
                .collect(java.util.stream.Collectors.toList());

        assigned.sort(Comparator
                .comparingInt((Ticket t) -> t.getBusinessPriority().score()).reversed()
                .thenComparing(Ticket::getId));
        return assigned;
    }

    public List<Ticket> viewTicketsAsDeveloper(String devUsername, String currentTimestamp) {
        LocalDate now = LocalDate.parse(currentTimestamp);

        List<Ticket> tickets = repo.getAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.OPEN)
                .filter(t -> t.getAssignedTo() == null || t.getAssignedTo().isEmpty())
                .filter(t -> milestoneRepo.isDevAssignedToTicketMilestone(t.getId(), devUsername))
                .collect(Collectors.toList());

        applyMilestoneEscalation(tickets, now);

        tickets.sort(Comparator.comparing(Ticket::getCreatedAt).thenComparingInt(Ticket::getId));
        return tickets;
    }

    public String undoAssignTicket(String devUsername, int ticketId, String timestamp) {
        var opt = repo.findById(ticketId);
        if (opt.isEmpty()) {
            return "Ticket not found.";
        }

        Ticket t = opt.get();

        if (t.getAssignedTo() == null || t.getAssignedTo().isEmpty()
                || !devUsername.equals(t.getAssignedTo())) {
            return "Cannot undo assignment.";
        }

        t.unassign();
        t.setStatus(TicketStatus.OPEN);

        t.logDeAssigned(devUsername, timestamp);

        return null;
    }

    private boolean canHandle(ExpertiseArea dev, ExpertiseArea req) {
        if (dev == null || req == null) return false;
        if (dev == ExpertiseArea.FULLSTACK) return true;

        return switch (req) {
            case DB -> dev == ExpertiseArea.DB || dev == ExpertiseArea.BACKEND;
            case BACKEND -> dev == ExpertiseArea.BACKEND || dev == ExpertiseArea.DEVOPS;
            case FRONTEND -> dev == ExpertiseArea.FRONTEND || dev == ExpertiseArea.DESIGN;
            case DEVOPS -> dev == ExpertiseArea.DEVOPS;
            case DESIGN -> dev == ExpertiseArea.DESIGN;
            case FULLSTACK -> dev == ExpertiseArea.FULLSTACK;
        };
    }

    private String requiredExpertiseList(ExpertiseArea req) {
        if (req == null) return "";
        return switch (req) {
            case DB -> "BACKEND, DB, FULLSTACK";
            case BACKEND -> "BACKEND, DEVOPS, FULLSTACK";
            case FRONTEND -> "FRONTEND, DESIGN, FULLSTACK";
            case DEVOPS -> "DEVOPS, FULLSTACK";
            case DESIGN -> "DESIGN, FULLSTACK";
            case FULLSTACK -> "FULLSTACK";
        };
    }

    private String requiredSeniorityList(BusinessPriority prio) {
        if (prio == null) return null;
        return switch (prio) {
            case HIGH -> "MID, SENIOR";
            case CRITICAL -> "SENIOR";
            default -> null;
        };
    }

    private boolean hasRequiredSeniority(SeniorityLevel devLevel, BusinessPriority prio) {
        if (prio == null) return true;
        if (devLevel == null) return false;

        return switch (prio) {
            case HIGH -> devLevel == SeniorityLevel.MID || devLevel == SeniorityLevel.SENIOR;
            case CRITICAL -> devLevel == SeniorityLevel.SENIOR;
            default -> true;
        };
    }

    public String addComment(String username, int ticketId, String comment, String timestamp) {
        Optional<Ticket> opt = repo.findById(ticketId);
        if (opt.isEmpty()) {
            return null;
        }

        Ticket t = opt.get();

        if (t.isAnonymous()) {
            return "Comments are not allowed on anonymous tickets.";
        }

        if (comment == null || comment.length() < 10) {
            return "Comment must be at least 10 characters long.";
        }

        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            return "User not found.";
        }

        Role role = userOpt.get().getRole();

        if (role != Role.REPORTER && role != Role.DEVELOPER) {
            return "User not allowed.";
        }

        if (role == Role.REPORTER && t.getStatus() == TicketStatus.CLOSED) {
            return "Reporters cannot comment on CLOSED tickets.";
        }

        if (role == Role.DEVELOPER) {
            if (!username.equals(t.getAssignedTo())) {
                return "Ticket " + ticketId + " is not assigned to the developer " + username + ".";
            }
        }

        if (role == Role.REPORTER) {
            if (!username.equals(t.getReportedBy())) {
                return "Reporter " + username + " cannot comment on ticket " + ticketId + ".";
            }
        }

        t.addComment(username, comment, timestamp);
        return null;
    }

    public String undoAddComment(String username, int ticketId, String timestamp) {
        Optional<Ticket> opt = repo.findById(ticketId);
        if (opt.isEmpty()) {
            return null;
        }

        Ticket t = opt.get();

        if (t.isAnonymous()) {
            return "Comments are not allowed on anonymous tickets.";
        }

        boolean removed = t.undoLastCommentBy(username);
        if (!removed) {
            return null;
        }

        return null;
    }

    public String changeStatus(String devUsername, String timestamp, int ticketId) {
        var opt = repo.findById(ticketId);
        if (opt.isEmpty()) {
            return "Ticket not found.";
        }

        Ticket t = opt.get();

        if (t.getAssignedTo() == null || t.getAssignedTo().isEmpty()
                || !devUsername.equals(t.getAssignedTo())) {
            return "Ticket " + ticketId + " is not assigned to developer " + devUsername + ".";
        }

        TicketStatus from = t.getStatus();
        TicketStatus to = from.next();

        if (from != to) {
            t.setStatus(to);
            t.logStatusChanged(from, to, devUsername, timestamp);
        }

        if (to == TicketStatus.CLOSED) {
            t.setClosedAt(LocalDate.parse(timestamp));
        }

        return null;
    }

    public String undoChangeStatus(String devUsername, String timestamp, int ticketId) {
        var opt = repo.findById(ticketId);
        if (opt.isEmpty()) {
            return "Ticket not found.";
        }

        Ticket t = opt.get();

        if (t.getAssignedTo() == null || t.getAssignedTo().isEmpty()
                || !devUsername.equals(t.getAssignedTo())) {
            return "Ticket " + ticketId + " is not assigned to developer " + devUsername + ".";
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

    public List<Ticket> viewTicketHistoryTickets(String devUsername) {
        return repo.getAll().stream()
                .filter(t -> wasEverAssignedTo(t, devUsername))
                .sorted(Comparator.comparingInt(Ticket::getId))
                .toList();
    }

    private boolean wasEverAssignedTo(Ticket t, String devUsername) {
        for (var a : t.getActions()) {
            Object action = a.get("action");
            Object by = a.get("by");
            if ("ASSIGNED".equals(action) && devUsername.equals(by)) {
                return true;
            }
        }
        return false;
    }
}
