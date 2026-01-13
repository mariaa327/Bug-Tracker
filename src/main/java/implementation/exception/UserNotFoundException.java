package implementation.exception;

public class UserNotFoundException extends AppException{
    public UserNotFoundException(String username) {
        super("The user " + username + " does not exist.");
    }
}
