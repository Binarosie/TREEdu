package vn.hcmute.edu.userservice.constants;

/**
 * Constants for error and success messages used throughout the auth service.
 */
public final class Messages {

    private Messages() {
        // Prevent instantiation
    }

    // Error Messages
    public static final class Error {
        public static final String USERNAME_EMAIL_ALREADY_IN_USE = "Username or email already in use";
        public static final String VALIDATION_FAILED = "Validation failed";
        public static final String INVALID_REQUEST = "Invalid request";
        public static final String MALFORMED_JSON = "Malformed JSON";
        public static final String UNAUTHORIZED = "Unauthorized";
        public static final String INTERNAL_SERVER_ERROR = "Internal server error";
        public static final String DEFAULT_ERROR = "Error";
    }

    // Success Messages
    public static final class Success {
        public static final String OPERATION_SUCCESS = "Success";
    }

    public static final class Member {
        public static final String CREATED = "Member created successfully.";
        public static final String UPDATED = "Member updated successfully.";
        public static final String DELETED = "Member deleted successfully.";
        public static final String FETCHED = "Member fetched successfully.";
        public static final String LIST_FETCHED = "Members fetched successfully.";
        public static final String EXPERIENCE_STATS = "Member experience statistics fetched successfully.";
    }

    public static final class Supporter {
        public static final String CREATED = "Supporter created successfully.";
        public static final String UPDATED = "Supporter updated successfully.";
        public static final String DELETED = "Supporter deleted successfully.";
        public static final String FETCHED = "Supporter fetched successfully.";
        public static final String LIST_FETCHED = "HR list fetched successfully.";
    }

    public static final class Admin {
        public static final String CREATED = "Admin created successfully.";
        public static final String UPDATED = "Admin updated successfully.";
        public static final String DELETED = "Admin deleted successfully.";
        public static final String FETCHED = "Admin fetched successfully.";
        public static final String LIST_FETCHED = "Admin list fetched successfully.";
    }

    public static final class Common {
        public static final String SUCCESS = "Operation completed successfully.";
        public static final String FAILED = "Operation failed.";
    }
}