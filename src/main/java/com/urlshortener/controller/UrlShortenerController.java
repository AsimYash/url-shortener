package com.urlshortener.controller;

import com.urlshortener.dto.CreateUrlRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UrlShortenerController — REST API Controller
 *
 * PURPOSE: Handles all API endpoints under /api/urls.
 * This is the HTTP layer — it ONLY deals with:
 *   1. Parsing HTTP requests
 *   2. Delegating to the service layer
 *   3. Returning HTTP responses
 *
 * NO business logic lives here. That belongs in UrlShortenerService.
 *
 * @RestController = @Controller + @ResponseBody
 *   - @Controller    → Registers this class as a Spring MVC controller
 *   - @ResponseBody  → Every method's return value is serialized to JSON
 *                      (instead of treating it as a view name)
 *
 * @RequestMapping("/api/urls") → All methods in this class are prefixed with /api/urls
 *
 * @RequiredArgsConstructor → Lombok generates constructor injection for `service`
 *
 * SWAGGER ANNOTATIONS:
 * @Tag          → Groups these endpoints under "URL Shortener API" in Swagger UI
 * @Operation    → Describes what each endpoint does
 * @ApiResponses → Documents possible HTTP responses
 */
@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "URL Shortener API", description = "Endpoints for creating and managing shortened URLs")
public class UrlShortenerController {

    private final UrlShortenerService urlShortenerService;

    /**
     * POST /api/urls
     * Create a new short URL.
     *
     * @Valid — Triggers validation of the request body using Bean Validation annotations.
     *          If validation fails, Spring throws MethodArgumentNotValidException
     *          which our GlobalExceptionHandler catches and returns a 400 response.
     *
     * ResponseEntity<UrlResponse> — Wraps the response with control over HTTP status.
     *   We return 201 CREATED for successful creation (not 200 OK).
     *   HTTP 201 = "A new resource was created as a result of this request."
     *
     * @RequestBody — Spring reads the JSON body and deserializes it into CreateUrlRequest
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new short URL", description = "Creates a shortened URL from a long URL. Optionally provide a custom short code.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Short URL created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request (validation error)"),
        @ApiResponse(responseCode = "409", description = "Custom short code already in use")
    })
    public ResponseEntity<UrlResponse> createShortUrl(@Valid @RequestBody CreateUrlRequest request) {
        log.info("API: POST /api/urls — Creating short URL for: {}", request.getOriginalUrl());

        UrlResponse response = urlShortenerService.createShortUrl(request);

        // 201 Created with the created resource in the body
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/urls
     * Get all shortened URLs.
     *
     * Returns 200 OK with a list of all URL mappings.
     * In a real production app, this would be paginated.
     */
    @GetMapping
    @Operation(summary = "Get all URLs", description = "Returns a list of all shortened URL mappings")
    @ApiResponse(responseCode = "200", description = "List of all URLs")
    public ResponseEntity<List<UrlResponse>> getAllUrls() {
        log.info("API: GET /api/urls — Fetching all URLs");

        List<UrlResponse> urls = urlShortenerService.getAllUrls();

        return ResponseEntity.ok(urls);  // 200 OK
    }

    /**
     * GET /api/urls/{shortCode}
     * Get details about a specific short URL.
     *
     * @PathVariable — Extracts {shortCode} from the URL path.
     * Example: GET /api/urls/abc123 → shortCode = "abc123"
     *
     * Note: This is for INFO only. The actual redirect is handled by RedirectController.
     */
    @GetMapping("/{shortCode}")
    @Operation(summary = "Get URL details", description = "Returns details and statistics for a specific short URL")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "URL details found"),
        @ApiResponse(responseCode = "404", description = "Short URL not found")
    })
    public ResponseEntity<UrlResponse> getUrlDetails(
            @Parameter(description = "The short code to look up")
            @PathVariable String shortCode) {

        log.info("API: GET /api/urls/{} — Fetching URL details", shortCode);

        UrlResponse response = urlShortenerService.getUrlDetails(shortCode);

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/urls/{shortCode}
     * Deactivate (soft-delete) a short URL.
     *
     * Returns 204 No Content — success but no response body.
     * HTTP 204 = "The request succeeded but there's nothing to return."
     */
    @DeleteMapping("/{shortCode}")
    @Operation(summary = "Deactivate a short URL", description = "Marks a short URL as inactive so it no longer redirects")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "URL deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Short URL not found")
    })
    public ResponseEntity<Void> deactivateUrl(
            @Parameter(description = "The short code to deactivate")
            @PathVariable String shortCode) {

        log.info("API: DELETE /api/urls/{} — Deactivating URL", shortCode);

        urlShortenerService.deactivateUrl(shortCode);

        return ResponseEntity.noContent().build();  // 204 No Content
    }
}
