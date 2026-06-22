-- ============================================================
-- URL SHORTENER — MySQL Database Schema
-- ============================================================
-- Run this file to set up your database from scratch.
-- Command: mysql -u root -p < schema.sql
-- ============================================================

-- Create the database if it doesn't exist
-- CHARACTER SET utf8mb4 supports ALL Unicode characters including emojis
-- COLLATE utf8mb4_unicode_ci = case-insensitive comparison
CREATE DATABASE IF NOT EXISTS urlshortener
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Switch to using the urlshortener database
USE urlshortener;

-- Drop existing table if re-running (useful during development)
-- WARNING: This deletes all data! Remove this line in production.
DROP TABLE IF EXISTS url_mappings;

-- ── Main URL Mappings Table ─────────────────────────────────
CREATE TABLE url_mappings (

    -- Primary key: auto-increments (1, 2, 3, ...)
    -- BIGINT supports up to ~9.2 quintillion rows
    id BIGINT NOT NULL AUTO_INCREMENT,

    -- The original long URL
    -- VARCHAR(2048) = up to 2048 characters (max practical URL length)
    -- NOT NULL = required
    original_url VARCHAR(2048) NOT NULL,

    -- The short code (e.g., "abc123")
    -- VARCHAR(20) = max 20 characters
    -- NOT NULL = always required
    -- UNIQUE = no two rows can have the same short_code
    short_code VARCHAR(20) NOT NULL,

    -- Click counter: how many times this URL was visited
    -- DEFAULT 0 = starts at zero
    -- BIGINT for very popular URLs with millions of clicks
    click_count BIGINT DEFAULT 0,

    -- When this URL mapping was created
    -- NOT NULL = always recorded
    created_at DATETIME NOT NULL,

    -- Optional expiration date/time
    -- NULL = never expires (most URLs won't have this set)
    expires_at DATETIME NULL,

    -- Optional display title
    title VARCHAR(255) NULL,

    -- Whether this URL is active
    -- TINYINT(1) = MySQL's way of storing boolean (1=true, 0=false)
    -- DEFAULT 1 = active by default
    is_active TINYINT(1) DEFAULT 1,

    -- Define primary key constraint
    PRIMARY KEY (id),

    -- UNIQUE constraint on short_code (enforced at DB level)
    -- Even if application code fails, the DB will reject duplicate codes
    UNIQUE KEY uk_short_code (short_code),

    -- INDEX on short_code for fast lookups (the most common query)
    -- Every redirect query searches by short_code, so this index is critical
    INDEX idx_short_code (short_code),

    -- INDEX on is_active for filtered queries
    INDEX idx_is_active (is_active),

    -- Composite index: finding active mappings by short code
    -- Covers the exact query: WHERE short_code = ? AND is_active = 1
    INDEX idx_short_code_active (short_code, is_active)

) ENGINE=InnoDB          -- InnoDB supports transactions and foreign keys
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Stores URL shortener mappings between short codes and original URLs';

-- ── Sample Data for Testing ─────────────────────────────────
-- Uncomment to insert test data:
/*
INSERT INTO url_mappings (original_url, short_code, click_count, created_at, title, is_active)
VALUES
    ('https://www.google.com', 'google', 42, NOW(), 'Google Search', 1),
    ('https://www.github.com', 'github', 18, NOW(), 'GitHub', 1),
    ('https://spring.io/projects/spring-boot', 'springbt', 7, NOW(), 'Spring Boot Docs', 1),
    ('https://www.expired-link.com', 'expired', 3, NOW(), 'Expired URL', 1),
    ('https://www.inactive.com', 'inactive', 1, NOW(), 'Inactive URL', 0);
*/

-- ── Useful Queries for Debugging ────────────────────────────
-- View all mappings:
--   SELECT * FROM url_mappings ORDER BY created_at DESC;
--
-- Find by short code:
--   SELECT * FROM url_mappings WHERE short_code = 'abc123';
--
-- Top 10 most clicked:
--   SELECT short_code, original_url, click_count
--   FROM url_mappings
--   ORDER BY click_count DESC LIMIT 10;
--
-- Find expired URLs:
--   SELECT * FROM url_mappings WHERE expires_at < NOW() AND expires_at IS NOT NULL;
--
-- Total clicks:
--   SELECT SUM(click_count) AS total_clicks FROM url_mappings WHERE is_active = 1;
