package implementation.repository.impl;

import implementation.domain.milestone.Milestone;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryMilestoneRepository {
    private final List<Milestone> milestones = new ArrayList<>();

    public void add(Milestone milestone) {
        milestones.add(milestone);
    }

    public List<Milestone> getAllForUser(String username) {
        return milestones.stream()
                .filter(m -> m.getCreatedBy().equals(username) || m.getAssignedDevs().contains(username))
                .sorted(Comparator.comparing(Milestone::getDueDate).thenComparing(Milestone::getName))
                .collect(Collectors.toList());
    }

    public boolean isMilestoneBlocked(String milestoneName) {
        for (Milestone m : milestones) {
            List<String> bf = m.getBlockingFor();
            if (bf != null && bf.contains(milestoneName))
                return true;
        }
        return false;
    }

    public String getMilestoneNameByTicket(Integer ticketId) {
        for (Milestone m : milestones) {
            if (m.getTickets().contains(ticketId))
                return m.getName();
        }
        return null;
    }

    public Optional<Milestone> findByTicketId(int ticketId) {
        for (Milestone m : milestones) {
            if (m.getTickets().contains(ticketId))
                return Optional.of(m);
        }
        return Optional.empty();
    }

    public boolean isDevAssignedToTicketMilestone(int ticketId, String devUsername) {
        return findByTicketId(ticketId)
                .map(m -> m.getAssignedDevs().contains(devUsername))
                .orElse(false);
    }
}
