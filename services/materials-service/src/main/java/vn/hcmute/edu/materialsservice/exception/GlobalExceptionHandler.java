package vn.hcmute.edu.materialsservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.hcmute.edu.materialsservice.Dto.response.*;
import vn.hcmute.edu.materialsservice.Dto.response.BadRequestError;
import vn.hcmute.edu.materialsservice.Dto.response.ErrorResponse;
import vn.hcmute.edu.materialsservice.Dto.response.ForbiddenError;
import vn.hcmute.edu.materialsservice.Dto.response.UnauthorizedError;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý FlashcardNotFoundException
     */
    @ExceptionHandler(FlashcardNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleFlashcardNotFoundException(
            FlashcardNotFoundException ex) {
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Xử lý FlashcardAlreadyExistsException
     */
    @ExceptionHandler(FlashcardAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleFlashcardAlreadyExistsException(
            FlashcardAlreadyExistsException ex) {
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.CONFLICT.value(),
                ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Xử lý InvalidFlashcardDataException
     */
    @ExceptionHandler(InvalidFlashcardDataException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidFlashcardDataException(
            InvalidFlashcardDataException ex) {
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý Validation Exception (từ @Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Dữ liệu không hợp lệ")
                .data(errors)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý tất cả exception khác (Generic Exception)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Đã xảy ra lỗi: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Xử lý IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // @ExceptionHandler(Exception.class)
    // public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    // // Xử lý lỗi không lường trước, mặc định 500
    // ErrorResponse error = new ErrorResponse("Internal Server Error", 500);
    // return ResponseEntity.status(500).body(error);
    // }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        UnauthorizedError error = new UnauthorizedError("Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        ForbiddenError error = new ForbiddenError("Access Denied");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Xử lý ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        ApiResponse<Object> response = ApiResponse.error(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // @ExceptionHandler(MethodArgumentNotValidException.class)
    // public ResponseEntity<ErrorResponse>
    // handleValidationErrors(MethodArgumentNotValidException ex) {
    // String message = ex.getBindingResult().getFieldErrors().stream()
    // .map(error -> error.getField() + ": " + error.getDefaultMessage())
    // .findFirst()
    // .orElse("Invalid request data");
    // return ResponseEntity.badRequest().body(new BadRequestError(message));
    // }

    @ExceptionHandler(BadRequestError.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestError ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(), ex.getStatusCode()));
    }

    @ExceptionHandler(IOException.class)
    public BadRequestError handleIO(IOException ex) {
        return new BadRequestError("Lỗi đọc file: " + ex.getMessage());
    }
}
