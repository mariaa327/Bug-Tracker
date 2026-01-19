package implementation.registry;

import implementation.domain.user.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * in-memory registry for User objects
 * users are stored by username for fast lookup
 */
public final class UserRegistry {
    private final Map<String, User> users = new HashMap<>();

    /**
     * adds a user to the registry
     * @param user the user to add; ignored if null or if username is
     *            null
     */
    public void add(final User user) {
        if (user == null || user.getUsername() == null) {
            return;
        }
        users.put(user.getUsername(), user);
    }

    /**
     * finds a user by username
     * @param username the username to search for
     * @return the user if found null otherwise
     */
    public User findByUsername(final String username) {
        return users.get(username);
    }

    /**
     * returns all users currently stored in the registry
     * the returned collection is backed by the internal map values view
     * @return a collection containing all users
     */
    public Collection<User> getAll() {
        return users.values();
    }
}
