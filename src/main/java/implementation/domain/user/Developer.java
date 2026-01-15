package implementation.domain.user;

public class Developer extends User {
    public Developer(String username) {
        super(username, Role.DEVELOPER);
    }
}
