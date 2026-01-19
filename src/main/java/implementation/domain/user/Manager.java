package implementation.domain.user;

/**
 * represents a manager user
 */
public class Manager extends User {
    /**
     * creates a manager with the given username
     * @param username the manager username
     */
    public Manager(final String username) {
        super(username, Role.MANAGER);
    }
}
