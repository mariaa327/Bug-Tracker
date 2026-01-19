package implementation.domain.flow;

import java.time.LocalDate;

/**
 * stores global project state shared across services and commands
 * (testing phase start date and investorsLost flag)
 * implemented as a singleton so the entire application works with
 * one consistent context instance
 */

public final class ProjectContext {
    private LocalDate startDate;
    private boolean investorsLost = false;

    private static ProjectContext singleton;

    /**
     * creates the project context singleton
     */
    private ProjectContext() {

    }

    /**
     * returns the singleton instance
     * @return the singleton instance
     */
    public static ProjectContext getSingleton() {
        if (singleton == null) {
            singleton = new ProjectContext();
        }
        return singleton;
    }

    /**
     * resets the project context state
     */
    public void reset() {
        this.startDate = null;
        this.investorsLost = false;
    }

    /**
     * returns the start date
     * @return the start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * sets the start date only if it was not set before
     * @param date the date string to parse
     */
    public void setStartDateIfNull(final String date) {
        if (this.startDate == null) {
            this.startDate = LocalDate.parse(date);
        }
    }

    /**
     * returns whether investors are lost
     * @return true if investors are lost false otherwise
     */
    public boolean areInvestorsLost() {
        return investorsLost;
    }

    /**
     * sets the investors lost status
     * @param status the new investors lost status
     */
    public void setInvestorsLost(final boolean status) {
        this.investorsLost = status;
    }
}
