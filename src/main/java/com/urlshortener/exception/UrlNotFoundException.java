package com.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * UrlNotFoundException — Custom 404 Exception
 *
 * PURPOSE: Thrown when a short code is not found in the database.
 *
 * WHY CUSTOM EXCEPTIONS?
 * Instead of returning error responses manually in every controller method,
 * we throw a specific exception and let the GlobalExceptionHandler catch it.
 * This keeps controllers clean and centralizes error handling.
 *
 * @ResponseStatus — If this exception is uncaught and bubbles to Spring,
 * Spring will automatically return a 404 HTTP response.
 * (We also handle it in GlobalExceptionHandler for a richer response body.)
 *
 * extends RuntimeException — We use unchecked exceptions so callers
 * don't need to declare "throws UrlNotFoundException" everywhere.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UrlNotFoundException extends RuntimeException {

    public UrlNotFoundException(String message) {
        super(message);
    }
}
