package com.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * ApiError — Standardized Error Response DTO
 *
 * PURPOSE: Every error returned by our API has this consistent shape.
 * This makes it easy for frontend/API clients to handle errors uniformly.
 *
 * @JsonInclude(NON_NULL) — Jackson will NOT include fields that are null in the JSON.
 * So if there are no validation errors, the "fieldErrors" key won't appear in the response.
 *
 * Example JSON for a 404 error:
 * {
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Short code 'xyz' not found",
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 *
 * Example JSON for a 400 validation error:
 * {
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Validation failed",
 *   "timestamp": "2024-01-15T10:30:00",
 *   "fieldErrors": {
 *     "originalUrl": "URL must start with http:// or https://",
 *     "customCode": "Custom code can only contain letters and numbers"
 *   }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    /** HTTP status code (e.g., 400, 404, 409, 500) */
    private int status;

    /** Short error category (e.g., "Not Found", "Bad Request") */
    private String error;

    /** Human-readable explanation of what went wrong */
    private String message;

    /** When the error occurred */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * For validation errors: a map of fieldName → errorMessage.
     * Example: { "originalUrl": "must not be blank" }
     * null for non-validation errors (hidden from JSON by @JsonInclude)
     */
    private Map<String, String> fieldErrors;
}
