package vn.hcmute.edu.materialsservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.hcmute.edu.materialsservice.Dto.response.ApiResponse;


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
                ex.getMessage()
        );
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
                ex.getMessage()
        );
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
                ex.getMessage()
        );
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
                "Đã xảy ra lỗi: " + ex.getMessage()
        );
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
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
