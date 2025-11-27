package tutul.ex.QarzMukti.constants;

/**
 * Centralized API endpoint constants for the micro-lending platform.
 * This class provides a single source of truth for all API endpoints,
 * improving maintainability and preventing typos.
 * 
 * @author QarzMukti Team
 * @version 1.0
 */
public final class ApiEndpoints {

    private ApiEndpoints() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ==================== Base Paths ====================

    public static final String API_BASE = "/api";

    // ==================== Authentication Endpoints ====================

    public static final class Auth {
        private Auth() {
        }

        public static final String BASE = API_BASE + "/auth";
        public static final String REGISTER = "/register";
        public static final String LOGIN = "/login";
        public static final String REFRESH_TOKEN = "/refresh";
        public static final String LOGOUT = "/logout";

        // Full paths
        public static final String FULL_REGISTER = BASE + REGISTER;
        public static final String FULL_LOGIN = BASE + LOGIN;
        public static final String FULL_REFRESH_TOKEN = BASE + REFRESH_TOKEN;
        public static final String FULL_LOGOUT = BASE + LOGOUT;
    }

    // ==================== User Endpoints ====================

    public static final class Users {
        private Users() {
        }

        public static final String BASE = API_BASE + "/users";
        public static final String BY_ID = "/{userId}";
        public static final String DOCUMENTS_UPLOAD = "/{userId}/documents/upload";
        public static final String VERIFY = "/verify";
        public static final String PENDING_VERIFICATION = "/pending-verification";
        public static final String UPDATE_PASSWORD = "/{userId}/password";
        public static final String ASSIGN_ROLES = "/{userId}/roles";
        public static final String ASSIGN_AUTHORITIES = "/authorities/assign";
        public static final String SEARCH = "/search";

        // Full paths
        public static final String FULL_BY_ID = BASE + BY_ID;
        public static final String FULL_DOCUMENTS_UPLOAD = BASE + DOCUMENTS_UPLOAD;
        public static final String FULL_VERIFY = BASE + VERIFY;
        public static final String FULL_PENDING_VERIFICATION = BASE + PENDING_VERIFICATION;
        public static final String FULL_UPDATE_PASSWORD = BASE + UPDATE_PASSWORD;
        public static final String FULL_ASSIGN_ROLES = BASE + ASSIGN_ROLES;
        public static final String FULL_ASSIGN_AUTHORITIES = BASE + ASSIGN_AUTHORITIES;
        public static final String FULL_SEARCH = BASE + SEARCH;
    }

    // ==================== Role Endpoints ====================

    public static final class Roles {
        private Roles() {
        }

        public static final String BASE = API_BASE + "/roles";
        public static final String BY_ID = "/{roleId}";
        public static final String BY_NAME = "/name/{name}";
        public static final String EXISTS = "/exists/{name}";
        public static final String USER_COUNT = "/{roleId}/user-count";

        // Full paths
        public static final String FULL_BY_ID = BASE + BY_ID;
        public static final String FULL_BY_NAME = BASE + BY_NAME;
        public static final String FULL_EXISTS = BASE + EXISTS;
        public static final String FULL_USER_COUNT = BASE + USER_COUNT;
    }

    // ==================== Authority Endpoints ====================

    public static final class Authorities {
        private Authorities() {
        }

        public static final String BASE = API_BASE + "/authorities";
        public static final String BY_ID = "/{authorityId}";
        public static final String BY_NAME = "/name/{name}";
        public static final String BY_RESOURCE = "/resource/{resource}";
        public static final String ASSIGN_TO_ROLE = "/assign-to-role";
        public static final String BY_ROLE = "/role/{roleId}";
        public static final String BY_USER = "/user/{userId}";
        public static final String USER_HAS_AUTHORITY = "/user/{userId}/has/{authorityName}";

        // Full paths
        public static final String FULL_BY_ID = BASE + BY_ID;
        public static final String FULL_BY_NAME = BASE + BY_NAME;
        public static final String FULL_BY_RESOURCE = BASE + BY_RESOURCE;
        public static final String FULL_ASSIGN_TO_ROLE = BASE + ASSIGN_TO_ROLE;
        public static final String FULL_BY_ROLE = BASE + BY_ROLE;
        public static final String FULL_BY_USER = BASE + BY_USER;
        public static final String FULL_USER_HAS_AUTHORITY = BASE + USER_HAS_AUTHORITY;
    }

    // ==================== Loan Endpoints (Future) ====================

    public static final class Loans {
        private Loans() {
        }

        public static final String BASE = API_BASE + "/loans";
        public static final String BY_ID = "/{loanId}";
        public static final String APPLY = "/apply";
        public static final String APPROVE = "/{loanId}/approve";
        public static final String REJECT = "/{loanId}/reject";
        public static final String REPAYMENT = "/{loanId}/repayment";

        // Full paths
        public static final String FULL_BY_ID = BASE + BY_ID;
        public static final String FULL_APPLY = BASE + APPLY;
        public static final String FULL_APPROVE = BASE + APPROVE;
        public static final String FULL_REJECT = BASE + REJECT;
        public static final String FULL_REPAYMENT = BASE + REPAYMENT;
    }

    // ==================== Treasury Endpoints (Future) ====================

    public static final class Treasury {
        private Treasury() {
        }

        public static final String BASE = API_BASE + "/treasury";
        public static final String MINT = "/mint";
        public static final String ALLOCATE = "/allocate";
        public static final String BALANCE = "/balance";

        // Full paths
        public static final String FULL_MINT = BASE + MINT;
        public static final String FULL_ALLOCATE = BASE + ALLOCATE;
        public static final String FULL_BALANCE = BASE + BALANCE;
    }

    // ==================== Reconciliation Endpoints (Future) ====================

    public static final class Reconciliation {
        private Reconciliation() {
        }

        public static final String BASE = API_BASE + "/reconciliation";
        public static final String DAILY_REPORT = "/daily-report";
        public static final String MATCH = "/match";
        public static final String RESOLVE = "/resolve";

        // Full paths
        public static final String FULL_DAILY_REPORT = BASE + DAILY_REPORT;
        public static final String FULL_MATCH = BASE + MATCH;
        public static final String FULL_RESOLVE = BASE + RESOLVE;
    }
}
