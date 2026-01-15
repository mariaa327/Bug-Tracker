package implementation.command.commands.advanced;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.ticket.*;
import implementation.domain.user.*;
import implementation.dto.input.CommandInput;
import implementation.dto.input.InputLoader;
import implementation.dto.input.SearchFilters;
import implementation.repository.impl.InMemoryMilestoneRepository;
import implementation.repository.impl.InMemoryTicketRepository;
import implementation.repository.impl.JsonUserRepository;
import implementation.service.MilestoneService;
import implementation.service.TicketService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class SearchCommand implements Command {
    private final JsonUserRepository userRepo;
    private final TicketService ticketService;
    private final MilestoneService milestoneService;

    public SearchCommand(JsonUserRepository userRepo,
                         TicketService ticketService,
                         MilestoneService milestoneService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
        this.milestoneService = milestoneService;
    }

    @Override
    public CommandResult execute(CommandInput input) {
        var userOpt = userRepo.findByUsername(input.getUsername());
        if (userOpt.isEmpty()) {
            return new CommandResult(errorNode(input, "User not found."));
        }

        SearchFilters f = input.getFilters();
        String searchType = (f == null || f.getSearchType() == null) ? "" : f.getSearchType().trim();

        ObjectMapper mapper = InputLoader.getMapper();
        ObjectNode out = mapper.createObjectNode();
        out.put("command", input.getCommand());
        out.put("username", input.getUsername());
        out.put("timestamp", input.getTimestamp());
        out.put("searchType", searchType);

        ArrayNode results = mapper.createArrayNode();

        if ("DEVELOPER".equals(searchType)) {
            if (userOpt.get().getRole() != Role.MANAGER) {
                return new CommandResult(errorNode(input,
                        "required role MANAGER; user role " + userOpt.get().getRole() + "."));
            }

            ExpertiseArea ea = (f.getExpertiseArea() == null) ? null : ExpertiseArea.valueOf(f.getExpertiseArea());
            SeniorityLevel sl = (f.getSeniority() == null) ? null : SeniorityLevel.valueOf(f.getSeniority());

            List<User> devs = userRepo.getAll().stream()
                    .filter(u -> u.getRole() == Role.DEVELOPER)
                    .filter(u -> ea == null || ea == u.getExpertiseArea())
                    .filter(u -> sl == null || sl == u.getSeniorityLevel())
                    .sorted(Comparator.comparing(User::getUsername))
                    .collect(Collectors.toList());

            for (User d : devs) {
                ObjectNode dn = mapper.createObjectNode();
                dn.put("username", d.getUsername());
                dn.put("expertiseArea", d.getExpertiseArea() == null ? "" : d.getExpertiseArea().name());
                dn.put("seniority", d.getSeniorityLevel() == null ? "" : d.getSeniorityLevel().name());
                dn.put("performanceScore", d.getPerformanceScore());
                dn.put("hireDate", d.getHireDate() == null ? "" : d.getHireDate().toString());
                results.add(dn);
            }

            out.set("results", results);
            return new CommandResult(out);
        }

        if ("TICKET".equals(searchType)) {
            List<Ticket> tickets = ticketService.getAllTickets().stream()
                    .filter(t -> t.getStatus() == TicketStatus.OPEN)
                    .collect(Collectors.toList());

            if (f.getType() != null) {
                TicketType tt = TicketType.valueOf(f.getType());
                tickets = tickets.stream().filter(t -> t.getType() == tt).collect(Collectors.toList());
            }

            if (f.getBusinessPriority() != null) {
                BusinessPriority bp = BusinessPriority.valueOf(f.getBusinessPriority());
                tickets = tickets.stream().filter(t -> t.getBusinessPriority() == bp).collect(Collectors.toList());
            }

            if (f.getCreatedAfter() != null) {
                LocalDate after = LocalDate.parse(f.getCreatedAfter());
                tickets = tickets.stream()
                        .filter(t -> t.getCreatedAt().isAfter(after))
                        .collect(Collectors.toList());
            }

            if (Boolean.TRUE.equals(f.getAvailableForAssignment())) {
                User u = userOpt.get();
                if (u.getRole() != Role.DEVELOPER) {
                    tickets = List.of();
                } else {
                    ExpertiseArea devEA = u.getExpertiseArea();
                    tickets = tickets.stream()
                            .filter(t -> t.getAssignedTo() == null || t.getAssignedTo().isEmpty())
                            .filter(t -> milestoneService.isDevAssignedToTicketMilestone(t.getId(), u.getUsername()))
                            .filter(t -> devEA == ExpertiseArea.FULLSTACK || t.getExpertiseArea() == devEA)
                            .collect(Collectors.toList());
                }
            }

            List<String> keywords = (f.getKeywords() == null) ? List.of() : f.getKeywords();
            boolean withMatchingWords = !keywords.isEmpty();
            if (withMatchingWords) {
                List<String> kwsLower = keywords.stream().map(s -> s.toLowerCase(Locale.ROOT)).toList();
                tickets = tickets.stream()
                        .filter(t -> {
                            String title = t.getTitle().toLowerCase(Locale.ROOT);
                            return kwsLower.stream().anyMatch(title::contains);
                        })
                        .collect(Collectors.toList());
            }

            tickets = tickets.stream()
                    .sorted(Comparator.comparingInt(Ticket::getId))
                    .collect(Collectors.toList());

            for (Ticket t : tickets) {
                ObjectNode tn = mapper.createObjectNode();
                tn.put("id", t.getId());
                tn.put("type", t.getType().name());
                tn.put("title", t.getTitle());
                tn.put("businessPriority", t.getBusinessPriority().name());
                tn.put("status", t.getStatus().name());
                tn.put("createdAt", t.getCreatedAt().toString());
                tn.put("solvedAt", t.getSolvedAt());
                tn.put("reportedBy", t.getReportedBy());

                if (withMatchingWords) {
                    ArrayNode mw = mapper.createArrayNode();
                    String titleLower = t.getTitle().toLowerCase(Locale.ROOT);
                    for (String kw : keywords) {
                        if (titleLower.contains(kw.toLowerCase(Locale.ROOT))) {
                            mw.add(kw);
                        }
                    }
                    tn.set("matchingWords", mw);
                }

                results.add(tn);
            }

            out.set("results", results);
            return new CommandResult(out);
        }

        out.set("results", results);
        return new CommandResult(out);
    }

    private ObjectNode errorNode(CommandInput input, String message) {
        ObjectMapper mapper = InputLoader.getMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("command", input.getCommand());
        node.put("username", input.getUsername());
        node.put("timestamp", input.getTimestamp());
        node.put("error", message);
        return node;
    }
}
