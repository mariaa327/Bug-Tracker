package implementation.dto.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchFilters {
    private String searchType;

    private String expertiseArea;
    private String seniority;

    private List<String> keywords;
    private String businessPriority;
    private String createdAfter;
    private Boolean availableForAssignment;
    private String type;
}
