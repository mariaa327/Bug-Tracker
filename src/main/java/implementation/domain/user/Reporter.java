package implementation.domain.user;

/**
 * represents a reporter user
 */
public class Reporter extends User {
    /**
     * creates a reporter with the given username
     * @param username the reporter username
     */
    public Reporter(final String username) {
        super(username, Role.REPORTER);
    }
}
