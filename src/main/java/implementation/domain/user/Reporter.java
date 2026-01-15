package implementation.domain.user;

public class Reporter extends User{
    public Reporter(String username){
        super(username, Role.REPORTER);
    }
}
