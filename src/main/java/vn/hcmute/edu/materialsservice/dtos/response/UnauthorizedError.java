package vn.hcmute.edu.materialsservice.dtos.response;


public class UnauthorizedError extends ErrorResponse {
    private static final int DEFAULT_STATUS = 401;
    private static final String DEFAULT_MESSAGE = "Unauthorized";

    public UnauthorizedError() {
        super(DEFAULT_MESSAGE, DEFAULT_STATUS);
    }

    public UnauthorizedError(String message) {
        super(message, DEFAULT_STATUS);
    }
}