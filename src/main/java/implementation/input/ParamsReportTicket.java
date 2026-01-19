package implementation.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParamsReportTicket {
    private String type;
    private String title;
    private String businessPriority;
    private String expertiseArea;
    private String description;
    private String reportedBy;

    private String expectedBehavior;
    private String actualBehavior;
    private String frequency;
    private String severity;
    private String environment;
    private Integer errorCode;

    private String businessValue;
    private String customerDemand;
    private Integer usabilityScore;
}
