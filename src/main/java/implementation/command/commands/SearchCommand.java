package implementation.command.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.command.Command;
import implementation.command.CommandResult;
import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.Ticket;
import implementation.domain.ticket.TicketStatus;
import implementation.domain.ticket.TicketType;
import implementation.domain.user.ExpertiseArea;
import implementation.domain.user.Role;
import implementation.domain.user.SeniorityLevel;
import implementation.domain.user.User;
import implementation.input.CommandInput;
import implementation.input.InputReader;
import implementation.input.SearchFilters;
import implementation.registry.UserRegistry;
import implementation.service.MilestoneService;
import implementation.service.TicketService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SearchCommand implements Command {
    private final UserRegistry userRepo;
    private final TicketService ticketService;
    private final MilestoneService milestoneService;

    /**
     * creates the search command
     * @param userRepo the user repository
     * @param ticketService the ticket service
     * @param milestoneService the milestone service
     */
    public SearchCommand(final UserRegistry userRepo,
                         final TicketService ticketService,
                         final MilestoneService milestoneService) {
        this.userRepo = userRepo;
        this.ticketService = ticketService;
        this.milestoneService = milestoneService;
    }

    /**
     * executes the search command
     * @param input the command input
     * @return a command result
     */
    @Override
    public CommandResult execute(final CommandInput input) {
        User caller = userRepo.findByUsername(input.getUsername());
        if (caller == null) {
            return new CommandResult(errorNode(input, "User not found."));
        }

        SearchFilters f = input.getFilters();
        String searchType = "";
        if (f != null && f.getSearchType() != null) {
            searchType = f.getSearchType().trim();
        }

        ObjectMapper mapper = InputReader.getMapper();
        ObjectNode out = mapper.createObjectNode();
        out.put("command", input.getCommand());
        out.put("username", input.getUsername());
        out.put("timestamp", input.getTimestamp());
        out.put("searchType", searchType);

        ArrayNode results = mapper.createArrayNode();

        if ("DEVELOPER".equals(searchType)) {
            return handleDeveloperSearch(input, caller, f, mapper, out, results);
        }

        if ("TICKET".equals(searchType)) {
            return handleTicketSearch(input, caller, f, mapper, out, results);
        }

        out.set("results", results);
        return new CommandResult(out);
    }

    /**
     * handles developer search
     * @param input the command input
     * @param caller the caller user
     * @param f the search filters
     * @param mapper the object mapper
     * @param out the output node
     * @param results the results array
     * @return a command result
     */
    private CommandResult handleDeveloperSearch(final CommandInput input,
                                                final User caller,
                                                final SearchFilters f,
                                                final ObjectMapper mapper,
                                                final ObjectNode out,
                                                final ArrayNode results) {
        if (caller.getRole() != Role.MANAGER) {
            return new CommandResult(errorNode(input,
                    "required role MANAGER; user role " + caller.getRole() + "."));
        }

        ExpertiseArea filterEA = null;
        if (f != null && f.getExpertiseArea() != null) {
            filterEA = ExpertiseArea.valueOf(f.getExpertiseArea());
        }

        SeniorityLevel filterSL = null;
        if (f != null && f.getSeniority() != null) {
            filterSL = SeniorityLevel.valueOf(f.getSeniority());
        }

        List<User> devs = new ArrayList<>();
        for (User u : userRepo.getAll()) {
            if (u.getRole() != Role.DEVELOPER) {
                continue;
            }

            if (filterEA != null && u.getExpertiseArea() != filterEA) {
                continue;
            }
            if (filterSL != null && u.getSeniorityLevel() != filterSL) {
                continue;
            }

            devs.add(u);
        }

        devs.sort((a, b) -> a.getUsername().compareTo(b.getUsername()));

        for (User d : devs) {
            ObjectNode dn = mapper.createObjectNode();
            dn.put("username", d.getUsername());
            dn.put("expertiseArea",
                    d.getExpertiseArea() == null ? "" : d.getExpertiseArea().name());
            dn.put("seniority",
                    d.getSeniorityLevel() == null ? "" : d.getSeniorityLevel().name());
            dn.put("performanceScore", d.getPerformanceScore());
            dn.put("hireDate",
                    d.getHireDate() == null ? "" : d.getHireDate().toString());
            results.add(dn);
        }

        out.set("results", results);
        return new CommandResult(out);
    }

    /**
     * handles ticket search
     * @param input the command input
     * @param caller the caller user
     * @param f the search filters
     * @param mapper the object mapper
     * @param out the output node
     * @param results the results array
     * @return a command result
     */
    private CommandResult handleTicketSearch(final CommandInput input,
                                             final User caller,
                                             final SearchFilters f,
                                             final ObjectMapper mapper,
                                             final ObjectNode out,
                                             final ArrayNode results) {

        List<Ticket> tickets = new ArrayList<>();
        for (Ticket t : ticketService.getAllTickets()) {
            if (t.getStatus() == TicketStatus.OPEN) {
                tickets.add(t);
            }
        }

        if (f != null && f.getType() != null) {
            TicketType tt = TicketType.valueOf(f.getType());
            List<Ticket> filtered = new ArrayList<>();
            for (Ticket t : tickets) {
                if (t.getType() == tt) {
                    filtered.add(t);
                }
            }
            tickets = filtered;
        }

        if (f != null && f.getBusinessPriority() != null) {
            BusinessPriority bp = BusinessPriority.valueOf(f.getBusinessPriority());
            List<Ticket> filtered = new ArrayList<>();
            for (Ticket t : tickets) {
                if (t.getBusinessPriority() == bp) {
                    filtered.add(t);
                }
            }
            tickets = filtered;
        }

        if (f != null && f.getCreatedAfter() != null) {
            LocalDate after = LocalDate.parse(f.getCreatedAfter());
            List<Ticket> filtered = new ArrayList<>();
            for (Ticket t : tickets) {
                if (t.getCreatedAt() != null && t.getCreatedAt().isAfter(after)) {
                    filtered.add(t);
                }
            }
            tickets = filtered;
        }

        if (f != null && Boolean.TRUE.equals(f.getAvailableForAssignment())) {
            if (caller.getRole() != Role.DEVELOPER) {
                tickets = new ArrayList<>();
            } else {
                ExpertiseArea devEA = caller.getExpertiseArea();
                List<Ticket> filtered = new ArrayList<>();

                for (Ticket t : tickets) {
                    String assignedTo = t.getAssignedTo();
                    boolean unassigned = (assignedTo == null || assignedTo.isEmpty());
                    if (!unassigned) {
                        continue;
                    }

                    boolean inMyMilestone = milestoneService
                            .isDevAssignedToTicketMilestone(
                                    t.getId(),
                                    caller.getUsername()
                            );
                    if (!inMyMilestone) {
                        continue;
                    }

                    if (devEA != ExpertiseArea.FULLSTACK && t.getExpertiseArea() != devEA) {
                        continue;
                    }

                    filtered.add(t);
                }

                tickets = filtered;
            }
        }

        List<String> keywords = new ArrayList<>();
        if (f != null && f.getKeywords() != null) {
            keywords.addAll(f.getKeywords());
        }

        boolean withMatchingWords = !keywords.isEmpty();
        if (withMatchingWords) {
            List<String> lower = new ArrayList<>();
            for (String k : keywords) {
                if (k != null) {
                    lower.add(k.toLowerCase(Locale.ROOT));
                }
            }

            List<Ticket> filtered = new ArrayList<>();
            for (Ticket t : tickets) {
                String title = (t.getTitle() == null) ? "" : t.getTitle().toLowerCase(Locale.ROOT);

                boolean ok = false;
                for (String k : lower) {
                    if (k != null && !k.isEmpty() && title.contains(k)) {
                        ok = true;
                        break;
                    }
                }

                if (ok) {
                    filtered.add(t);
                }
            }
            tickets = filtered;
        }

        tickets.sort((a, b) -> Integer.compare(a.getId(), b.getId()));

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
                String titleLower =
                        (t.getTitle() == null) ? "" : t.getTitle().toLowerCase(Locale.ROOT);

                for (String kw : keywords) {
                    if (kw == null) {
                        continue;
                    }
                    String k = kw.toLowerCase(Locale.ROOT);
                    if (!k.isEmpty() && titleLower.contains(k)) {
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

    /**
     * builds the error node
     * @param input the command input
     * @param message the error message
     * @return the error node
     */
    private ObjectNode errorNode(final CommandInput input, final String message) {
        ObjectMapper mapper = InputReader.getMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("command", input.getCommand());
        node.put("username", input.getUsername());
        node.put("timestamp", input.getTimestamp());
        node.put("error", message);
        return node;
    }
}
