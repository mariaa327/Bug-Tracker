package implementation.dto.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public final class CommandInput {
    private String command;
    private String username;
    private String timestamp;

    private String name;
    private String dueDate;
    private List<String> blockingFor;
    private List<Integer> tickets;
    private List<String> assignedDevs;
    private Integer ticketID;

    private ParamsReportTicket params;
}
