package com.urlshortener.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

/**
 * ShortCodeGenerator — Utility Component
 *
 * PURPOSE: Generates unique, random short codes for URLs.
 *
 * ALGORITHM:
 * We use Base62 encoding (characters 0-9, a-z, A-Z = 62 characters).
 * A 6-character Base62 code gives 62^6 = ~56 billion possible combinations.
 * That's more than enough for any URL shortener.
 *
 * WHY BASE62 (not UUID, not random numbers)?
 * - UUIDs are 36 chars — too long for a "short" URL
 * - Pure numbers are guessable (sequential IDs)
 * - Base62 is URL-safe (no special characters like +, /, =)
 * - Case-sensitive doubles the search space vs Base36
 *
 * EXAMPLE OUTPUT: "a8bX2k", "Tz9mQr", "0xKpL3"
 *
 * @Component — Marks this as a Spring-managed bean.
 *              Spring creates one instance and injects it wherever needed.
 *              We can use @Autowired or constructor injection to get it.
 */
@Component
public class ShortCodeGenerator {

    // All valid characters in our short codes
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // Default length of generated short codes
    private static final int DEFAULT_LENGTH = 6;

    /**
     * SecureRandom vs Random:
     * - java.util.Random is fast but predictable (not suitable for security-sensitive use)
     * - java.security.SecureRandom uses OS-level entropy (truly unpredictable)
     * For URL shorteners, SecureRandom prevents users from guessing other people's URLs.
     */
    private final SecureRandom random = new SecureRandom();

    /**
     * Generate a random short code of the default length (6 characters).
     *
     * @return A random 6-character alphanumeric string
     */
    public String generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * Generate a random short code of specified length.
     *
     * HOW IT WORKS:
     * 1. Create a StringBuilder to accumulate characters
     * 2. For each position, pick a random index into CHARACTERS
     * 3. Append that character
     * 4. Return the result as a String
     *
     * @param length Number of characters in the code
     * @return A random alphanumeric string of the specified length
     */
    public String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            // random.nextInt(62) → random number from 0 to 61 (inclusive)
            // CHARACTERS.charAt(index) → picks that character from our alphabet
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
