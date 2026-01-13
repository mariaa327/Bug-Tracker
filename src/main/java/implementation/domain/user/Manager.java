package implementation.domain.user;

public class Manager extends User{
    public Manager() {
        super();
    }
    public Manager(String username) {
        super(username, Role.MANAGER);
    }
}
