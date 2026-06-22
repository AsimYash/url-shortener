package com.urlshortener.service;

import com.urlshortener.dto.CreateUrlRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.entity.UrlMapping;
import com.urlshortener.exception.ShortCodeAlreadyExistsException;
import com.urlshortener.exception.UrlExpiredException;
import com.urlshortener.exception.UrlNotFoundException;
import com.urlshortener.repository.UrlMappingRepository;
import com.urlshortener.util.ShortCodeGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UrlShortenerService — Business Logic Layer
 *
 * PURPOSE: Contains ALL the business rules and logic for the URL shortener.
 * Controllers call this service; this service calls the repository.
 *
 * LAYERED ARCHITECTURE:
 * HTTP Request → Controller → Service → Repository → Database
 *                                ↑
 *                          You are here
 *
 * WHY A SEPARATE SERVICE LAYER?
 * - Controllers should ONLY handle HTTP (parse request, call service, return response)
 * - Repositories should ONLY handle database queries
 * - Services handle the actual LOGIC: validation, business rules, transformations
 * - This makes code testable: we can test service logic without HTTP or a real database
 *
 * @Service      — Marks this as a Spring service bean (specialization of @Component)
 * @Slf4j        — Lombok generates: private static final Logger log = ...
 * @RequiredArgsConstructor — Lombok generates a constructor with all `final` fields.
 *                            Spring uses this constructor for dependency injection.
 *                            This is the PREFERRED way to inject dependencies in Spring
 *                            (vs @Autowired field injection which makes testing harder).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UrlShortenerService {

    // ── Injected Dependencies ─────────────────────────────────────────────────

    /** Database access — injected via constructor by Spring */
    private final UrlMappingRepository repository;

    /** Short code generator utility — injected via constructor */
    private final ShortCodeGenerator shortCodeGenerator;

    /**
     * @Value reads from application.properties or environment variables.
     * We use this to build the full short URL (e.g., "https://myapp.com").
     * Set APP_BASE_URL=https://your-domain.com in your environment.
     */
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /** Maximum attempts to generate a unique code before giving up */
    private static final int MAX_GENERATION_ATTEMPTS = 10;

    // ── Public Service Methods ────────────────────────────────────────────────

    /**
     * Create a new short URL mapping.
     *
     * BUSINESS RULES:
     * 1. If customCode is provided → use it, but ensure it's not already taken
     * 2. If no customCode → auto-generate a unique code
     * 3. Save to DB and return the full response
     *
     * @Transactional — If anything fails (exception thrown), the entire DB operation
     *                  is rolled back. Ensures we never save a partial/inconsistent record.
     *
     * @param request The DTO containing the URL and optional settings
     * @return UrlResponse with the short URL and all details
     */
    @Transactional
    public UrlResponse createShortUrl(CreateUrlRequest request) {
        log.info("Creating short URL for: {}", request.getOriginalUrl());

        String shortCode;

        if (request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
            // --- Path 1: User wants a custom short code ---
            shortCode = request.getCustomCode().trim();

            // Check if someone already claimed this code
            if (repository.existsByShortCode(shortCode)) {
                throw new ShortCodeAlreadyExistsException(
                    "Short code '" + shortCode + "' is already in use. Please choose another."
                );
            }
            log.info("Using custom short code: {}", shortCode);

        } else {
            // --- Path 2: Auto-generate a unique short code ---
            shortCode = generateUniqueCode();
            log.info("Generated short code: {}", shortCode);
        }

        // Build the entity using Lombok's Builder pattern
        UrlMapping urlMapping = UrlMapping.builder()
                .originalUrl(request.getOriginalUrl().trim())
                .shortCode(shortCode)
                .title(request.getTitle())
                .expiresAt(request.getExpiresAt())
                // clickCount and isActive have @Builder.Default values (0 and true)
                .build();

        // Save to database — Spring JPA executes INSERT INTO url_mappings (...)
        UrlMapping saved = repository.save(urlMapping);
        log.info("Saved URL mapping with ID: {}", saved.getId());

        // Convert entity to response DTO and return
        return toResponse(saved);
    }

    /**
     * Resolve a short code to its original URL (for redirect).
     *
     * BUSINESS RULES:
     * 1. Find the active mapping for this code (throw 404 if not found)
     * 2. Check expiration (throw 410 Gone if expired)
     * 3. Increment the click count atomically
     * 4. Return the original URL
     *
     * @Transactional — The read + increment must be atomic to avoid race conditions
     *
     * @param shortCode The 6-character code from the URL
     * @return The original long URL to redirect to
     */
    @Transactional
    public String resolveUrl(String shortCode) {
        log.info("Resolving short code: {}", shortCode);

        // Find the mapping — findActiveByShortCode returns only is_active = true records
        UrlMapping mapping = repository.findActiveByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(
                    "Short URL '" + shortCode + "' not found or has been deactivated."
                ));

        // Check if the URL has expired
        if (mapping.getExpiresAt() != null && mapping.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Short code '{}' has expired at {}", shortCode, mapping.getExpiresAt());
            throw new UrlExpiredException(
                "Short URL '" + shortCode + "' expired on " + mapping.getExpiresAt()
            );
        }

        // Atomically increment the click counter in the database
        repository.incrementClickCount(mapping.getId());
        log.info("Incremented click count for code: {}", shortCode);

        return mapping.getOriginalUrl();
    }

    /**
     * Get details about a specific short URL (for the API, not for redirect).
     *
     * @param shortCode The short code to look up
     * @return Full UrlResponse with all metadata
     */
    public UrlResponse getUrlDetails(String shortCode) {
        log.info("Fetching details for short code: {}", shortCode);

        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(
                    "Short URL '" + shortCode + "' not found."
                ));

        return toResponse(mapping);
    }

    /**
     * Get all URL mappings (for admin/dashboard purposes).
     *
     * @return List of all URL responses
     */
    public List<UrlResponse> getAllUrls() {
        log.info("Fetching all URL mappings");

        return repository.findAll()
                .stream()
                .map(this::toResponse)          // Convert each entity to DTO
                .collect(Collectors.toList());
    }

    /**
     * Deactivate a short URL (soft delete — keeps the record but marks inactive).
     *
     * WHY SOFT DELETE?
     * If we hard-delete a record, we lose analytics data (click history).
     * Soft delete lets us keep the data while making the URL inaccessible.
     *
     * @param shortCode The code to deactivate
     */
    @Transactional
    public void deactivateUrl(String shortCode) {
        log.info("Deactivating short code: {}", shortCode);

        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(
                    "Short URL '" + shortCode + "' not found."
                ));

        mapping.setIsActive(false);
        repository.save(mapping);
        log.info("Deactivated short code: {}", shortCode);
    }

    // ── Private Helper Methods ─────────────────────────────────────────────────

    /**
     * Generate a unique short code that doesn't already exist in the database.
     *
     * We try up to MAX_GENERATION_ATTEMPTS times. The probability of collision
     * is extremely low (62^6 = 56 billion possibilities), but we handle it gracefully.
     *
     * @return A unique short code
     * @throws RuntimeException if we can't find a unique code in 10 attempts
     */
    private String generateUniqueCode() {
        for (int attempt = 1; attempt <= MAX_GENERATION_ATTEMPTS; attempt++) {
            String code = shortCodeGenerator.generate();

            if (!repository.existsByShortCode(code)) {
                return code;  // Found a unique code!
            }

            log.warn("Short code collision on attempt {}: {}", attempt, code);
        }

        // This should almost never happen in practice
        throw new RuntimeException(
            "Failed to generate a unique short code after " + MAX_GENERATION_ATTEMPTS + " attempts."
        );
    }

    /**
     * Convert a UrlMapping entity to a UrlResponse DTO.
     *
     * This is called every time we need to return data to the client.
     * We compute the full shortUrl by combining baseUrl + shortCode.
     *
     * @param mapping The database entity
     * @return The DTO safe to return to clients
     */
    private UrlResponse toResponse(UrlMapping mapping) {
        return UrlResponse.builder()
                .id(mapping.getId())
                .originalUrl(mapping.getOriginalUrl())
                .shortCode(mapping.getShortCode())
                .shortUrl(baseUrl + "/" + mapping.getShortCode())  // Computed field
                .clickCount(mapping.getClickCount())
                .createdAt(mapping.getCreatedAt())
                .expiresAt(mapping.getExpiresAt())
                .title(mapping.getTitle())
                .isActive(mapping.getIsActive())
                .build();
    }
}
