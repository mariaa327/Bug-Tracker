package implementation.registry;

import implementation.domain.ticket.Ticket;

import java.util.ArrayList;
import java.util.List;

/**
 * in-memory registry for Ticket objects
 * this registry stores tickets in a simple list and provides basic operations
 * such as id generation, add, list and lookup by id
 */
public final class TicketRegistry {
    private final List<Ticket> tickets = new ArrayList<>();
    private int nextId = 0;

    /**
     * generates and returns the next ticket id
     * @return the next unique ticket id
     */
    public int nextId() {
        int id = nextId;
        nextId++;
        return id;
    }

    /**
     * adds a ticket to the registry
     * @param t the ticket to add, if null, nothing is added
     */
    public void add(final Ticket t) {
        if (t == null) {
            return;
        }
        tickets.add(t);
    }

    /**
     * returns a copy of all tickets currently stored in the registry
     * @return a new list containing all tickets
     */
    public List<Ticket> getAll() {
        return new ArrayList<>(tickets);
    }

    /**
     * finds a ticket by its id
     * @param id the ticket id to search for
     * @return the ticket if found, null otherwise
     */
    public Ticket findById(final int id) {
        for (Ticket t : tickets) {
            if (t != null && t.getId() == id) {
                return t;
            }
        }
        return null;
    }
}
