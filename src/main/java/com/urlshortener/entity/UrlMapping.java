package com.urlshortener.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * UrlMapping — JPA Entity
 *
 * PURPOSE: Represents a single row in the `url_mappings` database table.
 * Each object of this class maps directly to one record in MySQL.
 *
 * JPA ANNOTATIONS EXPLAINED:
 *   @Entity      — Marks this class as a database entity (tells Hibernate to manage it)
 *   @Table       — Specifies the exact table name in MySQL
 *   @Id          — Marks the primary key field
 *   @GeneratedValue — Auto-increments the ID (MySQL handles this)
 *   @Column      — Customizes column properties (nullable, unique, length)
 *
 * LOMBOK ANNOTATIONS EXPLAINED:
 *   @Getter      — Generates getters for all fields (getId(), getOriginalUrl(), etc.)
 *   @Setter      — Generates setters for all fields
 *   @Builder     — Enables builder pattern: UrlMapping.builder().originalUrl("...").build()
 *   @NoArgsConstructor — Generates empty constructor (required by JPA)
 *   @AllArgsConstructor — Generates constructor with all fields (needed by @Builder)
 */
@Entity
@Table(name = "url_mappings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlMapping {

    /**
     * Primary key — auto-incremented by MySQL.
     * Strategy.IDENTITY = use MySQL's AUTO_INCREMENT.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The original long URL provided by the user.
     * nullable = false → MySQL NOT NULL constraint
     * length = 2048 → supports very long URLs
     */
    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    /**
     * The unique short code (e.g., "abc123").
     * unique = true → MySQL UNIQUE constraint (no two rows can share a code)
     * nullable = false → always required
     * length = 20 → short codes are never more than 20 chars
     */
    @Column(name = "short_code", unique = true, nullable = false, length = 20)
    private String shortCode;

    /**
     * How many times this short URL has been visited/clicked.
     * Defaults to 0 when a new mapping is created.
     */
    @Column(name = "click_count")
    @Builder.Default
    private Long clickCount = 0L;

    /**
     * When this short URL was created.
     * updatable = false → once set, this value never changes in the DB.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Optional expiration date. If null, the URL never expires.
     * If set and the date has passed, we return a 410 Gone response.
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * An optional human-readable title for this URL (for display purposes).
     */
    @Column(name = "title", length = 255)
    private String title;

    /**
     * Whether this mapping is active or has been soft-deleted.
     * "active" URLs are accessible; "inactive" ones return 404.
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * @PrePersist lifecycle hook.
     * This method runs automatically BEFORE a new entity is saved to the DB.
     * We use it to set the createdAt timestamp so we never forget to set it.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
