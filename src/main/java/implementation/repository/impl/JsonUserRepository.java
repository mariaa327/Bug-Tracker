package implementation.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import implementation.domain.user.Role;
import implementation.domain.user.User;
import implementation.dto.input.UserInput;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JsonUserRepository {
    private final Map<String, User> users = new HashMap<>();

    public JsonUserRepository(String path, ObjectMapper mapper) {
        try {
            UserInput[] arr = mapper.readValue(new File(path), UserInput[].class);
            if (arr == null)
                return;

            for (UserInput ui : arr) {
                if (ui == null)
                    continue;

                String username = trim(ui.getUsername());
                String roleStr = trim(ui.getRole());
                if (username.isEmpty() || roleStr.isEmpty())
                    continue;

                Role role;
                try {
                    role = Role.valueOf(roleStr);
                } catch (Exception e) {
                    continue;
                }

                User u = new User(username, role);
                u.setSubordinates(ui.getSubordinates());

                users.put(username, u);
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
