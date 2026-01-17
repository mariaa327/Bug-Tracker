package implementation.domain.user;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public abstract class User {
    private String username;
    private Role role;

    private ExpertiseArea expertiseArea;
    private SeniorityLevel seniorityLevel;

    private double performanceScore = 0.0;
    private LocalDate hireDate;

    private List<String> subordinates = new ArrayList<>();
    private final List<String> notifications = new ArrayList<>();

    public User() {}

    protected User(String username,Role role){
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }

    public ExpertiseArea getExpertiseArea() {
        return expertiseArea;
    }

    public void setExpertiseArea(ExpertiseArea expertiseArea) {
        this.expertiseArea = expertiseArea;
    }

    public SeniorityLevel getSeniorityLevel() {
        return seniorityLevel;
    }

    public void setSeniorityLevel(SeniorityLevel seniorityLevel) {
        this.seniorityLevel = seniorityLevel;
    }

    public double getPerformanceScore() {
        return performanceScore;
    }
    public void setPerformanceScore(double performanceScore) {
        this.performanceScore = performanceScore;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }
    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public List<String> getSubordinates() {
        if (subordinates == null) return Collections.emptyList();
        return subordinates;
    }

    public void setSubordinates(List<String> subs) {
        this.subordinates = (subs == null) ? new ArrayList<>() : new ArrayList<>(subs);
    }

    public void update(String message) {
        if (message == null) return;
        notifications.add(message);
    }

    public List<String> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    public void clearNotifications() {
        notifications.clear();
    }
}
