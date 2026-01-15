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

    private String expertiseArea;

    private String seniority;

    private Double performanceScore;
    private String hireDate;
}
