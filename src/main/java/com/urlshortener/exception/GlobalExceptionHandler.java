package com.urlshortener.exception;

import com.urlshortener.dto.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler — Centralized Error Handling
 *
 * PURPOSE: Catches all exceptions thrown anywhere in the application
 * and converts them into clean, consistent JSON error responses.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * This tells Spring: "This class handles exceptions from all @RestController classes".
 *
 * WITHOUT this class:
 * - Spring would return a generic Whitelabel Error Page (HTML) for errors
 * - Each controller would need its own try/catch blocks
 * - Error responses would be inconsistent
 *
 * WITH this class:
 * - All errors return our ApiError JSON format
 * - Controllers stay clean (no try/catch needed)
 * - Error handling is in one place (easy to maintain)
 *
 * @Slf4j — Lombok generates: private static final Logger log = LoggerFactory.getLogger(...)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles: Short code not found in database
     * HTTP Status: 404 Not Found
     */
    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ApiError> handleUrlNotFound(UrlNotFoundException ex) {
        log.warn("URL not found: {}", ex.getMessage());

        ApiError error = ApiError.builder()
                .status(HttpStatus.NOT_FOUND.value())    // 404
                .error("Not Found")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles: Custom short code already taken
     * HTTP Status: 409 Conflict
     */
    @ExceptionHandler(ShortCodeAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleShortCodeConflict(ShortCodeAlreadyExistsException ex) {
        log.warn("Short code conflict: {}", ex.getMessage());

        ApiError error = ApiError.builder()
                .status(HttpStatus.CONFLICT.value())     // 409
                .error("Conflict")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handles: URL has expired
     * HTTP Status: 410 Gone
     */
    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ApiError> handleUrlExpired(UrlExpiredException ex) {
        log.warn("URL expired: {}", ex.getMessage());

        ApiError error = ApiError.builder()
                .status(HttpStatus.GONE.value())         // 410
                .error("Gone")
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.GONE).body(error);
    }

    /**
     * Handles: @Valid annotation failures (invalid request body fields)
     * HTTP Status: 400 Bad Request
     *
     * When @Valid fails, Spring throws MethodArgumentNotValidException.
     * We extract each field's error message and return them all in a map.
     *
     * Example response:
     * {
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "Validation failed. Check 'fieldErrors' for details.",
     *   "fieldErrors": {
     *     "originalUrl": "URL must start with http:// or https://",
     *     "customCode": "Custom code can only contain letters and numbers"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        // Collect all field-level validation errors into a map
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())  // 400
                .error("Bad Request")
                .message("Validation failed. Check 'fieldErrors' for details.")
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles: Any unexpected exception not caught by handlers above
     * HTTP Status: 500 Internal Server Error
     *
     * This is the "catch-all" handler. We log the full stack trace here
     * for debugging, but return a generic message to the client
     * (never expose internal error details to users in production!).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        // log.error logs the full stack trace — critical for debugging production issues
        log.error("Unexpected error occurred", ex);

        ApiError error = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())  // 500
                .error("Internal Server Error")
                .message("An unexpected error occurred. Please try again later.")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
