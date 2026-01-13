package implementation.dto.input;

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
    private String uiElementId;
    private Integer usabilityScore;
    private String suggestedFix;

    public ParamsReportTicket() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBusinessPriority() {
        return businessPriority;
    }

    public void setBusinessPriority(String businessPriority) {
        this.businessPriority = businessPriority;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getExpertiseArea() {
        return expertiseArea;
    }

    public void setExpertiseArea(String expertiseArea) {
        this.expertiseArea = expertiseArea;
    }

    public String getExpectedBehavior() {
        return expectedBehavior;
    }
    public void setExpectedBehavior(String expectedBehavior) {
        this.expectedBehavior = expectedBehavior;
    }

    public String getActualBehavior() {
        return actualBehavior;
    }
    public void setActualBehavior(String actualBehavior) {
        this.actualBehavior = actualBehavior;
    }

    public String getFrequency() {
        return frequency;
    }
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getSeverity() {
        return severity;
    }
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getEnvironment() {
        return environment;
    }
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getBusinessValue() {
        return businessValue;
    }
    public void setBusinessValue(String businessValue) {
        this.businessValue = businessValue;
    }

    public String getCustomerDemand() {
        return customerDemand;
    }
    public void setCustomerDemand(String customerDemand) {
        this.customerDemand = customerDemand;
    }

    public Integer getUsabilityScore() {
        return usabilityScore;
    }
    public void setUsabilityScore(Integer usabilityScore) {
        this.usabilityScore = usabilityScore;
    }
}
