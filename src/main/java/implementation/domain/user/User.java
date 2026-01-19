package implementation.domain.user;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * base class for all users in the system
 */
public abstract class User {
    private String username;
    private Role role;

    private ExpertiseArea expertiseArea;
    private SeniorityLevel seniorityLevel;

    private double performanceScore = 0.0;
    private LocalDate hireDate;

    private List<String> subordinates = new ArrayList<>();
    private final List<String> notifications = new ArrayList<>();

    /**
     * default constructor (used by serializers)
     */
    public User() { }

    /**
     * creates a user with a username and role
     * @param username the user username
     * @param role the user role
     */
    protected User(final String username, final Role role) {
        this.username = username;
        this.role = role;
    }

    /**
     * returns the username
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * sets the username
     * @param username the new username
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * returns the role
     * @return the role
     */
    public Role getRole() {
        return role;
    }

    /**
     * sets the role
     * @param role the new role
     */
    public void setRole(final Role role) {
        this.role = role;
    }

    /**
     * returns the expertise area
     * @return the expertise area
     */
    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    /**
     * sets the expertise area
     * @param expertiseArea the new expertise area
     */
    public void setExpertiseArea(final ExpertiseArea expertiseArea) {
        this.expertiseArea = expertiseArea;
    }

    /**
     * returns the seniority level
     * @return the seniority level
     */
    public SeniorityLevel getSeniorityLevel() {
        return seniorityLevel;
    }

    /**
     * sets the seniority level
     * @param seniorityLevel the new seniority level
     */
    public void setSeniorityLevel(final SeniorityLevel seniorityLevel) {
        this.seniorityLevel = seniorityLevel;
    }

    /**
     * returns the performance score
     * @return the performance score
     */
    public double getPerformanceScore() {
        return performanceScore;
    }

    /**
     * sets the performance score
     * @param performanceScore the new performance score
     */
    public void setPerformanceScore(final double performanceScore) {
        this.performanceScore = performanceScore;
    }

    /**
     * returns the hire date
     * @return the hire date
     */
    public LocalDate getHireDate() {
        return hireDate;
    }

    /**
     * sets the hire date
     * @param hireDate the new hire date
     */
    public void setHireDate(final LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    /**
     * returns the subordinates list (never null)
     * @return the subordinates list
     */
    public List<String> getSubordinates() {
        if (subordinates == null) {
            return Collections.emptyList();
        }
        return subordinates;
    }

    /**
     * sets the subordinates list (stores a defensive copy)
     * @param subs the new subordinates list
     */
    public void setSubordinates(final List<String> subs) {
        this.subordinates = (subs == null) ? new ArrayList<>() : new ArrayList<>(subs);
    }

    /**
     * adds a notification message for this user
     * @param message the message to add
     */
    public void update(final String message) {
        if (message == null) {
            return;
        }
        notifications.add(message);
    }

    /**
     * returns an unmodifiable view of notifications
     * @return the notifications list
     */
    public List<String> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    /**
     * clears all notifications
     */
    public void clearNotifications() {
        notifications.clear();
    }
}
