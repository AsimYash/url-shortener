package com.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ShortCodeAlreadyExistsException — Custom 409 Conflict Exception
 *
 * Thrown when a user requests a custom short code that is already taken.
 * HTTP 409 Conflict = the request conflicts with the current state of the server.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ShortCodeAlreadyExistsException extends RuntimeException {

    public ShortCodeAlreadyExistsException(String message) {
        super(message);
    }
}
