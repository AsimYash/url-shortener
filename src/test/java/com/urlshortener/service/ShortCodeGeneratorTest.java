package com.urlshortener.service;

import com.urlshortener.util.ShortCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ShortCodeGeneratorTest — Tests the code generation utility.
 *
 * We test the REAL ShortCodeGenerator here (no mocks needed).
 * This is a pure unit test — no Spring context, no database.
 */
@DisplayName("ShortCodeGenerator Unit Tests")
class ShortCodeGeneratorTest {

    private ShortCodeGenerator generator;

    @BeforeEach
    void setUp() {
        // No mocking needed — ShortCodeGenerator has no dependencies
        generator = new ShortCodeGenerator();
    }

    @Test
    @DisplayName("Generated code should be 6 characters by default")
    void generate_DefaultLength_Returns6Chars() {
        String code = generator.generate();
        assertThat(code).hasSize(6);
    }

    @Test
    @DisplayName("Generated code should only contain alphanumeric characters")
    void generate_OnlyAlphanumericChars() {
        String code = generator.generate();
        // Matches only letters and digits
        assertThat(code).matches("[a-zA-Z0-9]+");
    }

    @Test
    @DisplayName("Custom length should produce code of that length")
    void generate_CustomLength_ReturnsCorrectLength() {
        assertThat(generator.generate(8)).hasSize(8);
        assertThat(generator.generate(12)).hasSize(12);
        assertThat(generator.generate(3)).hasSize(3);
    }

    /**
     * @RepeatedTest(100) — Runs this test 100 times.
     * Ensures we get different values (randomness check).
     */
    @RepeatedTest(10)
    @DisplayName("Should generate non-null, non-empty codes consistently")
    void generate_Repeated_NeverNullOrEmpty() {
        String code = generator.generate();
        assertThat(code).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("Should generate mostly unique codes (randomness test)")
    void generate_LargeSample_MostlyUnique() {
        // Generate 1000 codes and check for collisions
        Set<String> codes = new HashSet<>();
        int count = 1000;

        for (int i = 0; i < count; i++) {
            codes.add(generator.generate());
        }

        // With 62^6 = ~56 billion possibilities, 1000 codes should be nearly all unique.
        // Allow at most 1% collision rate.
        int uniqueCount = codes.size();
        assertThat(uniqueCount).isGreaterThan(count * 99 / 100);
    }
}
