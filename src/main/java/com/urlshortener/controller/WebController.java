package com.urlshortener.controller;

import com.urlshortener.dto.CreateUrlRequest;
import com.urlshortener.dto.UrlResponse;
import com.urlshortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * WebController — Thymeleaf Web UI Controller
 *
 * PURPOSE: Handles the web interface (HTML pages) for the URL shortener.
 *
 * DIFFERENCE FROM REST CONTROLLERS:
 * - REST controllers (@RestController) return JSON data for APIs
 * - Web controllers (@Controller) return view names → Thymeleaf renders HTML
 *
 * HOW THYMELEAF WORKS:
 * 1. Method returns a String like "index" or "dashboard"
 * 2. Spring MVC looks for src/main/resources/templates/index.html
 * 3. Thymeleaf processes the template (replaces th:* attributes with real values)
 * 4. The final HTML is sent to the browser
 *
 * Model — A map that passes data from the controller to the Thymeleaf template.
 * model.addAttribute("key", value) → accessible in HTML as ${key}
 *
 * @Controller (not @RestController) — We return view names, not JSON
 * @RequestMapping("/") → This controller handles the root and /home paths
 */
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
@Slf4j
public class WebController {

    private final UrlShortenerService urlShortenerService;

    /**
     * GET /
     * Show the main page with the URL shortening form.
     *
     * We add an empty CreateUrlRequest to the model so Thymeleaf
     * can bind the form fields to it (th:object="${urlRequest}").
     *
     * @param model Spring MVC Model — data bag passed to the template
     * @return "index" → renders templates/index.html
     */
    @GetMapping
    public String index(Model model) {
        log.info("Web: GET / — Loading home page");

        // Add empty form object for Thymeleaf form binding
        model.addAttribute("urlRequest", new CreateUrlRequest());

        return "index";  // → templates/index.html
    }

    /**
     * POST /shorten
     * Handle the URL shortening form submission.
     *
     * @Valid — Validates the form data using Bean Validation annotations on CreateUrlRequest
     * @ModelAttribute — Binds form fields to the CreateUrlRequest object
     * BindingResult — Holds validation errors (must immediately follow the validated param)
     * RedirectAttributes — Passes data across a redirect (using flash attributes)
     *
     * POST-REDIRECT-GET PATTERN:
     * After a successful POST, we REDIRECT to GET /result instead of returning the template.
     * Why? If the user refreshes the page, they'd resubmit the form without the redirect.
     * With PRG: POST → [redirect] → GET /result → user sees result without re-POST risk.
     */
    @PostMapping("/shorten")
    public String shortenUrl(
            @Valid @ModelAttribute("urlRequest") CreateUrlRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            org.springframework.ui.Model model) {

        log.info("Web: POST /shorten — Processing URL: {}", request.getOriginalUrl());

        // If validation failed, return to form with error messages
        if (bindingResult.hasErrors()) {
            log.warn("Web form validation failed: {}", bindingResult.getAllErrors());
            return "index";  // Return to index.html — Thymeleaf shows the errors
        }

        try {
            UrlResponse response = urlShortenerService.createShortUrl(request);

            // Flash attributes survive a redirect (stored in session briefly)
            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("result", response);

            log.info("Web: Successfully shortened URL. Short code: {}", response.getShortCode());

            // Redirect to /result (GET) — PRG Pattern
            return "redirect:/result";

        } catch (Exception e) {
            log.error("Web: Failed to shorten URL", e);

            // Add error message to model and re-show the form
            model.addAttribute("errorMessage", e.getMessage());
            return "index";
        }
    }

    /**
     * GET /result
     * Show the result page with the created short URL.
     *
     * Flash attributes from the redirect are automatically available in the model.
     * Thymeleaf can access them as ${success} and ${result}.
     */
    @GetMapping("/result")
    public String result(Model model) {
        log.info("Web: GET /result — Showing result page");

        // If someone navigates directly to /result without a flash attribute,
        // redirect them back to home
        if (!model.containsAttribute("success")) {
            return "redirect:/";
        }

        return "result";  // → templates/result.html
    }

    /**
     * GET /dashboard
     * Show a dashboard with all shortened URLs and their statistics.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        log.info("Web: GET /dashboard — Loading dashboard");

        List<UrlResponse> urls = urlShortenerService.getAllUrls();
        model.addAttribute("urls", urls);
        model.addAttribute("totalUrls", urls.size());
        model.addAttribute("totalClicks",
            urls.stream().mapToLong(UrlResponse::getClickCount).sum()
        );

        return "dashboard";  // → templates/dashboard.html
    }
}
