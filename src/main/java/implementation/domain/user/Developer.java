package implementation.domain.user;

public class Developer extends User {
    public Developer() {
        super();
    }
    public Developer(String username) {
        super(username, Role.DEVELOPER);
    }
}
