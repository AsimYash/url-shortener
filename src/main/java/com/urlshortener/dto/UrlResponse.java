package com.urlshortener.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * UrlResponse — Response DTO
 *
 * PURPOSE: Represents the JSON body returned to the client after creating or
 * fetching a short URL. This is what the API caller sees.
 *
 * WHY NOT RETURN THE ENTITY DIRECTLY?
 * 1. We may want to add computed fields (like the full shortUrl with domain).
 * 2. We can hide internal fields (like isActive, internal IDs, etc.).
 * 3. It separates the API contract from the database schema.
 *
 * Jackson (Spring's JSON library) will serialize all fields annotated
 * or accessible via getters into JSON automatically.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlResponse {

    /** The database primary key of this mapping */
    private Long id;

    /** The original long URL */
    private String originalUrl;

    /** The 6–8 character short code (e.g., "a8bX2k") */
    private String shortCode;

    /**
     * The full short URL including the server base URL.
     * Example: "https://myapp.com/a8bX2k"
     * This is a COMPUTED field — not stored in DB, calculated at request time.
     */
    private String shortUrl;

    /** How many times this short URL has been clicked */
    private Long clickCount;

    /** When the mapping was created */
    private LocalDateTime createdAt;

    /** When the mapping expires (null = never) */
    private LocalDateTime expiresAt;

    /** Optional display title */
    private String title;

    /** Whether this URL is currently active */
    private Boolean isActive;
}
