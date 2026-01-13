package implementation.dto.input;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserInput {
    private String username;
    private String role;

    private List<String> subordinates;

    @JsonAlias({"expertiseArea", "expertise", "expertise_area"})
    private String expertiseArea;

    @JsonAlias({"seniorityLevel", "seniority", "seniority_level", "level"})
    private String seniorityLevel;
}
