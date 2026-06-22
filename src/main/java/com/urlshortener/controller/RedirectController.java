package com.urlshortener.controller;

import com.urlshortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;

/**
 * RedirectController — Handles URL Redirection
 *
 * PURPOSE: When a user visits http://yourdomain.com/abc123,
 * this controller looks up "abc123" and redirects to the original URL.
 *
 * WHY SEPARATE FROM UrlShortenerController?
 * - UrlShortenerController is a @RestController → always returns JSON
 * - RedirectController needs to return an HTTP redirect (301/302 with Location header)
 * - Separating them keeps responsibilities clear and avoids configuration conflicts
 *
 * @Controller (not @RestController) — We need to return ResponseEntity with custom
 * HTTP headers, not just a JSON body. @Controller gives us that flexibility.
 *
 * REDIRECT TYPES:
 * 301 Moved Permanently — Browser caches the redirect (bad for analytics, good for SEO)
 * 302 Found (Temporary)  — Browser doesn't cache (good for analytics — click counts work)
 *
 * We use 302 here so every click goes through our server and increments the counter.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Redirect", description = "Handles short URL redirection to original URLs")
public class RedirectController {

    private final UrlShortenerService urlShortenerService;

    /**
     * GET /{shortCode}
     * Redirect from a short URL to the original long URL.
     *
     * FLOW:
     * 1. User visits https://yourapp.com/abc123
     * 2. This method is called with shortCode = "abc123"
     * 3. We look up the original URL in the database
     * 4. We increment the click counter
     * 5. We return a 302 redirect response with the Location header set
     * 6. The user's browser follows the redirect to the original URL
     *
     * HOW THE REDIRECT WORKS:
     * We return ResponseEntity with:
     *   - Status: 302 Found
     *   - Header: Location: https://www.original-long-url.com
     * The browser sees this and automatically navigates to the Location URL.
     *
     * ResponseEntity<Void> — "Void" because there's no body, just headers.
     */
    @GetMapping("/{shortCode}")
    @Operation(
        summary = "Redirect to original URL",
        description = "Looks up the short code, tracks the click, and redirects to the original URL"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "302", description = "Redirect to original URL"),
        @ApiResponse(responseCode = "404", description = "Short URL not found"),
        @ApiResponse(responseCode = "410", description = "Short URL has expired")
    })
    public ResponseEntity<Void> redirect(
            @Parameter(description = "The short code to resolve")
            @PathVariable String shortCode) {

        log.info("Redirect request for short code: {}", shortCode);

        // Resolve the short code → original URL (also increments click count)
        String originalUrl = urlShortenerService.resolveUrl(shortCode);

        log.info("Redirecting {} → {}", shortCode, originalUrl);

        // Build the redirect response
        // HttpHeaders lets us set response headers
        HttpHeaders headers = new HttpHeaders();

        // Location header tells the browser where to go
        headers.setLocation(URI.create(originalUrl));

        // 302 Found = temporary redirect (browser won't cache this)
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
