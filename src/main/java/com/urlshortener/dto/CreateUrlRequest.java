package com.urlshortener.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * CreateUrlRequest — Data Transfer Object (DTO)
 *
 * PURPOSE: Represents the JSON body sent by the client when creating a short URL.
 *
 * WHY USE A DTO INSTEAD OF THE ENTITY DIRECTLY?
 * 1. Security — We control exactly what fields the client can send.
 *    (We don't want clients setting clickCount or createdAt themselves!)
 * 2. Validation — We validate the incoming data BEFORE it touches the database.
 * 3. Decoupling — The API contract (DTO) can evolve independently of the DB schema (Entity).
 *
 * VALIDATION ANNOTATIONS:
 *   @NotBlank    — Field must not be null, empty, or only whitespace
 *   @Size        — Enforces min/max length
 *   @Pattern     — Field must match a regex
 *   The (message = "...") shows a user-friendly error message when validation fails.
 */
@Data                   // Lombok: generates @Getter + @Setter + @ToString + @EqualsAndHashCode
@Builder                // Enables: CreateUrlRequest.builder().originalUrl("...").build()
@NoArgsConstructor      // Empty constructor for JSON deserialization by Jackson
@AllArgsConstructor     // All-args constructor used by @Builder
public class CreateUrlRequest {

    /**
     * The long URL to shorten. Must be a valid HTTP/HTTPS URL.
     * @NotBlank ensures the field isn't empty.
     * @Pattern ensures it looks like a real URL.
     */
    @NotBlank(message = "Original URL is required")
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    @Pattern(
        regexp = "^(https?://).*",
        message = "URL must start with http:// or https://"
    )
    private String originalUrl;

    /**
     * Optional custom short code (e.g., user wants "my-brand" instead of "a8bX2k").
     * If left blank, the service will generate one automatically.
     * Must be 3–20 characters if provided, only letters and digits.
     */
    @Pattern(
    regexp = "^$|^[a-zA-Z0-9-_]{3,20}$",
    message = "Custom code must be between 3 and 20 characters"
)
    private String customCode;

    /**
     * Optional: a human-readable title for this shortened URL.
     */
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    /**
     * Optional expiration date/time.
     * If provided, the short URL will stop working after this date.
     * Format expected: "2024-12-31T23:59:59"
     */
    private LocalDateTime expiresAt;
}
