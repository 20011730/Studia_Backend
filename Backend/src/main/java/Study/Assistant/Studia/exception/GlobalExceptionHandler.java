package Study.Assistant.Studia.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException e) {
        return createErrorResponse(HttpStatus.CONFLICT, e.getMessage());
    }
    
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException e) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException e) {
        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("errors", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("Unexpected error occurred", e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
    
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        
        return ResponseEntity.status(status).body(response);
    }
}
