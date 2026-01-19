package implementation.domain.user;

/**
 * represents a developer user
 */
public class Developer extends User {
    /**
     * creates a developer user with the given username
     * @param username the developer username
     */
    public Developer(final String username) {
        super(username, Role.DEVELOPER);
    }
}
