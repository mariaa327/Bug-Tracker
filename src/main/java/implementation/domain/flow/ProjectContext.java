package implementation.domain.flow;

import java.time.LocalDate;

public class ProjectContext {
    private LocalDate startDate;
    private boolean investorsLost = false;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDateIfNull(String date) {
        if (this.startDate == null) {
            this.startDate = LocalDate.parse(date);
        }
    }

    public boolean areInvestorsLost() {
        return investorsLost;
    }

    public void setInvestorsLost(boolean status) {
        this.investorsLost = status;
    }
}
