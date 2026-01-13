package implementation.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import implementation.domain.ticket.Ticket;

import java.time.LocalDate;

public final class JsonOutputUtils {
    private JsonOutputUtils() { }

    public static ObjectNode ticketForViewTickets(ObjectMapper mapper, Ticket t) {
        ObjectNode node = mapper.createObjectNode();

        node.put("id", t.getId());
        node.put("type", t.getType().toString());
        node.put("title", t.getTitle());
        node.put("businessPriority", t.getBusinessPriority().toString());
        node.put("status", t.getStatus().toString());

        node.put("createdAt", safeDate(t.getCreatedAt()));

        node.put("assignedAt", safe(t.getAssignedAt()));
        node.put("solvedAt", safe(t.getSolvedAt()));
        node.put("assignedTo", safe(t.getAssignedTo()));
        node.put("reportedBy", safe(t.getReportedBy()));

        node.set("comments", mapper.valueToTree(t.getComments()));
        return node;
    }

    public static ObjectNode ticketForAssignedTickets(ObjectMapper mapper, Ticket t) {
        ObjectNode node = mapper.createObjectNode();

        node.put("id", t.getId());
        node.put("type", t.getType().toString());
        node.put("title", t.getTitle());
        node.put("businessPriority", t.getBusinessPriority().toString());
        node.put("status", t.getStatus().toString());

        node.put("createdAt", safeDate(t.getCreatedAt()));

        node.put("assignedAt", safe(t.getAssignedAt()));
        node.put("reportedBy", safe(t.getReportedBy()));

        node.set("comments", mapper.valueToTree(t.getComments()));
        return node;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String safeDate(LocalDate d) {
        return d == null ? "" : d.toString();
    }
}
