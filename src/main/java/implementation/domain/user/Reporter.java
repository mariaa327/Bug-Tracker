package implementation.domain.user;

public class Reporter extends User{
    public Reporter() {
        super();
    }
    public Reporter(String username){
        super(username, Role.REPORTER);
    }
}
