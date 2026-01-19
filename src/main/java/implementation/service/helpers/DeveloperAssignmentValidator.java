package implementation.service.helpers;

import implementation.domain.ticket.BusinessPriority;
import implementation.domain.ticket.Ticket;
import implementation.domain.user.ExpertiseArea;
import implementation.domain.user.SeniorityLevel;
import implementation.domain.user.User;

/**
 * helper used to validate if a developer is eligible to assign a ticket
 * based on expertise area and seniority constraints
 */
public final class DeveloperAssignmentValidator {

    /**
     * validates if a developer can assign a ticket
     * @param dev the developer user
     * @param t the ticket
     * @param ticketId the ticket id
     * @param devUsername the developer username
     * @return null if ok an error message otherwise
     */
    public String validateDevCanAssign(final User dev,
                                       final Ticket t,
                                       final int ticketId,
                                       final String devUsername) {
        if (!canHandle(dev.getExpertiseArea(), t.getExpertiseArea())) {
            return "Developer " + devUsername
                    + " cannot assign ticket " + ticketId
                    + " due to expertise area. Required: "
                    + requiredExpertiseList(t.getExpertiseArea())
                    + "; Current: " + dev.getExpertiseArea() + ".";
        }

        String seniorityReq = requiredSeniorityList(t.getBusinessPriority());
        if (seniorityReq != null
                && !hasRequiredSeniority(dev.getSeniorityLevel(), t.getBusinessPriority())) {
            return "Developer " + devUsername
                    + " cannot assign ticket " + ticketId
                    + " due to seniority level. Required: " + seniorityReq
                    + "; Current: " + dev.getSeniorityLevel() + ".";
        }

        return null;
    }

    /**
     * checks if developer expertise can handle required expertise
     * @param dev the developer expertise
     * @param required the required expertise
     * @return true if can handle false otherwise
     */
    public boolean canHandle(final ExpertiseArea dev, final ExpertiseArea required) {
        if (dev == null) {
            return false;
        }
        return dev.canHandle(required);
    }

    /**
     * builds required expertise list for error messages
     * @param req the required expertise
     * @return required expertise list string
     */
    public String requiredExpertiseList(final ExpertiseArea req) {
        if (req == null) {
            return "";
        }

        return switch (req) {
            case DB -> "BACKEND, DB, FULLSTACK";
            case BACKEND -> "BACKEND, DEVOPS, FULLSTACK";
            case FRONTEND -> "FRONTEND, DESIGN, FULLSTACK";
            case DEVOPS -> "DEVOPS, FULLSTACK";
            case DESIGN -> "DESIGN, FULLSTACK";
            case FULLSTACK -> "FULLSTACK";
        };
    }

    /**
     * returns the required seniority list for a priority
     * @param prio the business priority
     * @return the required seniority list or null if no restriction
     */
    public String requiredSeniorityList(final BusinessPriority prio) {
        if (prio == null) {
            return null;
        }

        return switch (prio) {
            case HIGH -> "MID, SENIOR";
            case CRITICAL -> "SENIOR";
            default -> null;
        };
    }

    /**
     * checks if developer seniority satisfies the priority requirement
     * @param devLevel the developer seniority
     * @param prio the ticket priority
     * @return true if ok false otherwise
     */
    public boolean hasRequiredSeniority(final SeniorityLevel devLevel,
                                        final BusinessPriority prio) {
        if (prio == null) {
            return true;
        }
        if (devLevel == null) {
            return false;
        }

        return switch (prio) {
            case HIGH -> devLevel == SeniorityLevel.MID
                    || devLevel == SeniorityLevel.SENIOR;
            case CRITICAL -> devLevel == SeniorityLevel.SENIOR;
            default -> true;
        };
    }
}
