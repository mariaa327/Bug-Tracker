package implementation.domain.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Manager extends User{
    private final List<String> subordinates = new ArrayList<>();

    public Manager() { super(); }
    public Manager(String username) {
        super(username, Role.MANAGER);
    }


}
