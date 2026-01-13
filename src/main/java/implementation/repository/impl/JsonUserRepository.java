package implementation.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import implementation.domain.user.*;
import implementation.dto.input.UserInput;

import java.io.File;
import java.util.*;

public class JsonUserRepository {
    private final Map<String, User> users = new HashMap<>();

    public JsonUserRepository(String path, ObjectMapper mapper) {
        try {
            UserInput[] usersArray = mapper.readValue(new File(path), UserInput[].class);
            List<UserInput> inputList = Arrays.asList(usersArray);

            for (UserInput input : inputList) {
                String username = input.getUsername();
                String roleStr = input.getRole();

                Role role = Role.valueOf(roleStr);

                User user = switch (role) {
                    case MANAGER -> new Manager(username);
                    case DEVELOPER -> new Developer(username);
                    case REPORTER -> new Reporter(username);
                };

                user.setRole(role);

                if (input.getExpertiseArea() != null && !input.getExpertiseArea().isEmpty()) {
                    user.setExpertiseArea(ExpertiseArea.valueOf(input.getExpertiseArea()));
                }
                if (input.getSeniorityLevel() != null && !input.getSeniorityLevel().isEmpty()) {
                    user.setSeniorityLevel(SeniorityLevel.valueOf(input.getSeniorityLevel()));
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
}
