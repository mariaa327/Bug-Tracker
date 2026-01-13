package implementation.dto.input;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class UserInput {
    private String username;
    private String role;
    private List<String> subordinates;
}
