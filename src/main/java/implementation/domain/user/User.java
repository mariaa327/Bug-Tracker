package implementation.domain.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {
    private String username;
    private Role role;
    private final List<String> subordinates = new ArrayList<>();

    public User() {}

    public User(String username, Role role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }
    public Role getRole() {
        return role;
    }

    public List<String> getSubordinates() {
        return Collections.unmodifiableList(subordinates);
    }

    public void setSubordinates(List<String> subs) {
        subordinates.clear();
        if (subs != null) subordinates.addAll(subs);
    }
}
