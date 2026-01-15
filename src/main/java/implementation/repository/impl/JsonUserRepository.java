package implementation.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import implementation.domain.user.*;
import implementation.dto.input.UserInput;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

public class JsonUserRepository {
    private final Map<String, User> users = new HashMap<>();

    public JsonUserRepository(String path, ObjectMapper mapper) {
        try {
            UserInput[] usersArray = mapper.readValue(new File(path), UserInput[].class);
            if (usersArray == null) {
                return;
            }

            for (UserInput ui : usersArray) {
                if (ui == null) continue;

                String username = safeTrim(ui.getUsername());
                String roleStr = safeTrim(ui.getRole());

                if (username.isEmpty() || roleStr.isEmpty()) {
                    continue;
                }

                Role role;
                try {
                    role = Role.valueOf(roleStr);
                } catch (IllegalArgumentException ex) {
                    continue;
                }

                User user = switch (role) {
                    case MANAGER -> new Manager(username);
                    case DEVELOPER -> new Developer(username);
                    case REPORTER -> new Reporter(username);
                };

                user.setRole(role);

                String exp = safeTrim(ui.getExpertiseArea());
                if (!exp.isEmpty()) {
                    try {
                        user.setExpertiseArea(ExpertiseArea.valueOf(exp));
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                String sen = safeTrim(ui.getSeniority());
                if (!sen.isEmpty()) {
                    try {
                        user.setSeniorityLevel(SeniorityLevel.valueOf(sen));
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                try {
                    Double ps = ui.getPerformanceScore();
                    if (ps != null) {
                        user.setPerformanceScore(ps);
                    }
                } catch (NoSuchMethodError | Exception ignored) {
                }

                try {
                    String hd = safeTrim(ui.getHireDate());
                    if (!hd.isEmpty()) {
                        user.setHireDate(LocalDate.parse(hd));
                    }
                } catch (NoSuchMethodError | Exception ignored) {
                }

                users.put(username, user);
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    public Collection<User> getAll() {
        return users.values();
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
