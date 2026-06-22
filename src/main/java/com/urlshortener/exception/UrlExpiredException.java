package com.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * UrlExpiredException — Custom 410 Gone Exception
 *
 * Thrown when a valid short code exists in the DB but its expiration date has passed.
 *
 * HTTP 410 Gone is the semantically correct status for this situation:
 * - 404 Not Found = we don't know about this resource
 * - 410 Gone = we KNEW about this resource but it's no longer available
 * This helps search engines and clients understand the URL existed but expired.
 */
@ResponseStatus(HttpStatus.GONE)
public class UrlExpiredException extends RuntimeException {

    public UrlExpiredException(String message) {
        super(message);
    }
}
