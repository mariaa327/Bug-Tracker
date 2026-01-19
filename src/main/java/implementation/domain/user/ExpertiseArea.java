package implementation.domain.user;

/**
 * defines the expertise areas for developers and provides support checks
 */
public enum ExpertiseArea {
    FRONTEND {
        @Override
        protected boolean supports(final ExpertiseArea required) {
            return required == FRONTEND || required == DESIGN;
        }
    },
    BACKEND {
        @Override
        protected boolean supports(final ExpertiseArea required) {
            return required == BACKEND || required == DB;
        }
    },
    DEVOPS {
        @Override
        protected boolean supports(final ExpertiseArea required) {
            return required == DEVOPS;
        }
    },
    DESIGN {
        @Override
        protected boolean supports(final ExpertiseArea required) {
            return required == DESIGN || required == FRONTEND;
        }
    },
    DB {
        @Override
        protected boolean supports(final ExpertiseArea required) {
            return required == DB || required == BACKEND;
        }
    },
    FULLSTACK {
        @Override
        protected boolean supports(final ExpertiseArea required) {
            return true;
        }
    };

    /**
     * checks if this expertise area can handle the required area
     * if required is null, it is considered handleable
     * @param required the required expertise area
     * @return true if this expertise can handle the required one
     */
    public final boolean canHandle(final ExpertiseArea required) {
        if (required == null) {
            return true;
        }
        return supports(required);
    }

    /**
     * checks if this expertise area supports the required one
     * @param required the required expertise area
     * @return true if the required area is supported
     */
    protected abstract boolean supports(ExpertiseArea required);
}
