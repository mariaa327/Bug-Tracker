package implementation.input;

import com.fasterxml.jackson.databind.ObjectMapper;
import implementation.domain.user.Developer;
import implementation.domain.user.ExpertiseArea;
import implementation.domain.user.Manager;
import implementation.domain.user.Reporter;
import implementation.domain.user.Role;
import implementation.domain.user.SeniorityLevel;
import implementation.domain.user.User;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserReader {

    /**
     * loads users from a JSON file path using the provided ObjectMapper
     * and converts each UserInput entry into a domain User instance
     * @param path the file system path to the JSON input containing users
     * @param mapper the Jackson ObjectMapper used to deserialize the JSON file
     * @return a list of parsed User objects; empty list if path/mapper are null
     * or parsing fails
     */
    public List<User> load(final String path, final ObjectMapper mapper) {
        List<User> out = new ArrayList<>();
        if (path == null || mapper == null) {
            return out;
        }

        try {
            UserInput[] usersArray = mapper.readValue(new File(path), UserInput[].class);
            if (usersArray == null) {
                return out;
            }

            for (UserInput ui : usersArray) {
                User u = parseOne(ui);
                if (u != null) {
                    out.add(u);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
        }

        return out;
    }

    /**
     * parses a single UserInput object into a concrete User instance
     * based on role and optional fields (expertise, seniority, performance,
     * hire date, subordinates)
     * @param ui the deserialized UserInput entry from the JSON input
     * @return a concrete User (Manager/Developer/Reporter) or null if required
     * fields are missing/invalid
     */
    private User parseOne(final UserInput ui) {
        if (ui == null) {
            return null;
        }

        String username = safeTrim(ui.getUsername());
        String roleStr = safeTrim(ui.getRole());
        if (username.isEmpty() || roleStr.isEmpty()) {
            return null;
        }

        Role role;
        try {
            role = Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            return null;
        }

        User user;
        if (role == Role.MANAGER) {
            user = new Manager(username);
        } else if (role == Role.DEVELOPER) {
            user = new Developer(username);
        } else {
            user = new Reporter(username);
        }

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

        Double ps = ui.getPerformanceScore();
        if (ps != null) {
            user.setPerformanceScore(ps);
        }

        String hd = safeTrim(ui.getHireDate());
        if (!hd.isEmpty()) {
            try {
                user.setHireDate(LocalDate.parse(hd));
            } catch (Exception ignored) {

            }
        }

        if (ui.getSubordinates() != null) {
            try {
                user.setSubordinates(ui.getSubordinates());
            } catch (Exception ignored) {

            }
        }

        return user;
    }

    /**
     * safely trims a string by returning an empty string for null values
     * and applying string trim otherwise
     * @param s the input string that may be null
     * @return the trimmed string or an empty string if s is null
     */
    private static String safeTrim(final String s) {
        return (s == null) ? "" : s.trim();
    }
}
