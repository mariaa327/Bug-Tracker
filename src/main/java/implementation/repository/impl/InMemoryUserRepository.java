package implementation.repository.impl;

import implementation.domain.user.User;

import java.util.HashMap;
import java.util.Map;

public class InMemoryUserRepository {
    private final Map<String, User> users = new HashMap<>();

    public void add(User u) {
        users.put(u.getUsername(), u);
    }

    public User get(String username) {
        return users.get(username);
    }
}
