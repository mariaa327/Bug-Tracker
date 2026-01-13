package implementation.repository.impl;

import implementation.domain.ticket.Ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryTicketRepository {
    private final List<Ticket> tickets = new ArrayList<>();
    private int nextId = 0;

    public int nextId() {
        return nextId++;
    }

    public void add(Ticket t) {
        tickets.add(t);
    }

    public List<Ticket> getAll() {
        return tickets;
    }

    public Optional<Ticket> findById(int id) {
        return tickets.stream().filter(t -> t.getId() == id).findFirst();
    }
}
