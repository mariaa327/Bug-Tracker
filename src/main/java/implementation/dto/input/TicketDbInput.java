package implementation.dto.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TicketDbInput {
    private int id;
    private String type;
    private String title;
    private String businessPriority;
    private String status;
    private String createdAt;
    private String reportedBy;
    private String assignedTo;
    private String assignedAt;
    private String solvedAt;
    private List<?> comments;
    private String expertiseArea;
    private String description;
}
