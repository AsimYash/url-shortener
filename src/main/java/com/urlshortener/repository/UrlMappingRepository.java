package com.urlshortener.repository;

import com.urlshortener.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * UrlMappingRepository — Spring Data JPA Repository
 *
 * PURPOSE: Provides database access (CRUD operations) for UrlMapping entities.
 *
 * HOW IT WORKS:
 * - We extend JpaRepository<UrlMapping, Long>
 *   → UrlMapping: the entity type
 *   → Long: the type of the primary key (@Id field)
 *
 * - Spring Data JPA AUTOMATICALLY provides these methods without any implementation:
 *   → save(entity)       — INSERT or UPDATE
 *   → findById(id)       — SELECT by primary key
 *   → findAll()          — SELECT all rows
 *   → delete(entity)     — DELETE a row
 *   → count()            — COUNT rows
 *   → existsById(id)     — SELECT EXISTS
 *
 * - Spring also auto-implements "derived query methods" by parsing method names:
 *   findByShortCode(code) → SELECT * FROM url_mappings WHERE short_code = ?
 *   existsByShortCode(code) → SELECT EXISTS(... WHERE short_code = ?)
 *
 * We NEVER write SQL for basic operations — Spring handles it.
 * For complex operations, we use @Query with JPQL (Java Persistence Query Language).
 *
 * @Repository — Marks this as a repository bean. Also enables Spring to translate
 *               database exceptions into DataAccessException (Spring's exception hierarchy).
 */
@Repository
public interface UrlMappingRepository extends JpaRepository<UrlMapping, Long> {

    /**
     * Find a URL mapping by its short code.
     * Auto-implemented by Spring: SELECT * FROM url_mappings WHERE short_code = ?
     *
     * Returns Optional<UrlMapping> — a container that may or may not hold a value.
     * This forces callers to handle the "not found" case explicitly, preventing NullPointerExceptions.
     */
    Optional<UrlMapping> findByShortCode(String shortCode);

    /**
     * Check if a short code already exists (for uniqueness validation).
     * Auto-implemented: SELECT COUNT(*) > 0 FROM url_mappings WHERE short_code = ?
     */
    boolean existsByShortCode(String shortCode);

    /**
     * Find a URL mapping by short code, but ONLY if it's active.
     *
     * @Query uses JPQL (not SQL): we reference the entity class (UrlMapping)
     * and field names (m.shortCode, m.isActive), NOT table/column names.
     *
     * This is equivalent to:
     * SELECT * FROM url_mappings WHERE short_code = ? AND is_active = true
     */
    @Query("SELECT m FROM UrlMapping m WHERE m.shortCode = :code AND m.isActive = true")
    Optional<UrlMapping> findActiveByShortCode(@Param("code") String shortCode);

    /**
     * Increment click count atomically using a direct UPDATE query.
     *
     * WHY NOT just do entity.setClickCount(clickCount + 1) and save()?
     * In high-traffic apps, two requests could both read clickCount = 5,
     * both add 1, and both save 6 — losing one click (race condition).
     * A direct UPDATE in the DB is atomic and avoids this.
     *
     * @Modifying — required for any INSERT/UPDATE/DELETE @Query
     * The query: UPDATE url_mappings SET click_count = click_count + 1 WHERE id = ?
     */
    @Modifying
    @Query("UPDATE UrlMapping m SET m.clickCount = m.clickCount + 1 WHERE m.id = :id")
    void incrementClickCount(@Param("id") Long id);
}
