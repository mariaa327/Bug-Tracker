package implementation.service;

import implementation.domain.flow.ProjectContext;
import implementation.domain.milestone.Milestone;
import implementation.domain.ticket.*;
import implementation.domain.user.Role;
import implementation.domain.user.User;
import implementation.repository.impl.InMemoryMilestoneRepository;
import implementation.repository.impl.InMemoryTicketRepository;
import implementation.repository.impl.JsonUserRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

public class TicketService {
    private final InMemoryTicketRepository repo;
    private final FlowService flowService;
    private final JsonUserRepository userRepo;
    private final InMemoryMilestoneRepository milestoneRepo;
    private final Map<Integer, LocalDate> lastEscalatedOn = new HashMap<>();


    public TicketService(InMemoryTicketRepository repo,
                         FlowService flowService,
                         JsonUserRepository userRepo,
                         InMemoryMilestoneRepository milestoneRepo) {
        this.repo = repo;
        this.flowService = flowService;
        this.userRepo = userRepo;
        this.milestoneRepo = milestoneRepo;
    }

    public String reportTicket(String timestamp, implementation.dto.input.ParamsReportTicket params) {
        String phaseError = flowService.validateTestingPhase(timestamp);
        if (phaseError != null) return phaseError;

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

        LocalDate createdAt = LocalDate.parse(timestamp);

        Ticket t = switch (type) {
            case BUG -> new BugTicket(params.getTitle(), priority, createdAt, reportedBy);
            case FEATURE_REQUEST -> new FeatureRequestTicket(params.getTitle(), priority, createdAt, reportedBy);
            case UI_FEEDBACK -> new UiFeedbackTicket(params.getTitle(), priority, createdAt, reportedBy);
        };

        t.setId(repo.nextId());
        repo.add(t);
        return null;
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

    public String assignTicket(String devUsername, String timestamp, int ticketId) {
        Optional<Ticket> opt = repo.findById(ticketId);
        if (opt.isEmpty()) return "Ticket not found.";

        Ticket t = opt.get();

        Optional<Milestone> mOpt = milestoneRepo.findByTicketId(ticketId);
        if (mOpt.isEmpty()) return "Ticket not found.";

        String milestoneName = mOpt.get().getName();

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
        if (devOpt.isEmpty() || devOpt.get().getRole() != Role.DEVELOPER) {
            return "The user " + devUsername + " does not exist.";
        }

        t.assignTo(devUsername, LocalDate.parse(timestamp));
        return null;
    }

    public List<Ticket> viewAssignedTickets(String devUsername, String timestamp) {
        List<Ticket> assigned = repo.getAll().stream()
                .filter(t -> devUsername.equals(t.getAssignedTo()))
                .collect(Collectors.toList());

        assigned.sort(Comparator
                .comparingInt((Ticket t) -> t.getBusinessPriority().score()).reversed()
                .thenComparing(Ticket::getId));
        return assigned;
    }

    public String undoAssignTicket(String devUsername, int ticketId, String timestamp) {
        Optional<Ticket> opt = repo.findById(ticketId);
        if (opt.isEmpty()) return "Ticket not found.";

        Ticket t = opt.get();

        if (t.getAssignedTo() == null || t.getAssignedTo().isEmpty()
                || !devUsername.equals(t.getAssignedTo())) {
            return "Cannot undo assignment.";
        }

        t.unassign();
        return null;
    }
}
