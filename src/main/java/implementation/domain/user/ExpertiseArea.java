package implementation.domain.user;

public enum ExpertiseArea {
    FRONTEND,
    BACKEND,
    DEVOPS,
    DESIGN,
    DB,
    FULLSTACK;

    public boolean canHandle(ExpertiseArea required) {
        if (required == null)
            return true;

        return switch (this) {
            case FULLSTACK -> true;

            case FRONTEND -> required == FRONTEND || required == DESIGN;
            case DESIGN -> required == DESIGN || required == FRONTEND;
            case BACKEND -> required == BACKEND || required == DB;
            case DB -> required == DB || required == BACKEND;
            case DEVOPS -> required == DEVOPS;
        };
    }
}
