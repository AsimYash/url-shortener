package com.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenApiConfig — Swagger/OpenAPI Documentation Configuration
 *
 * PURPOSE: Configures the auto-generated API documentation available at:
 *   - Swagger UI: http://localhost:8080/swagger-ui.html
 *   - OpenAPI JSON: http://localhost:8080/v3/api-docs
 *
 * HOW IT WORKS:
 * The springdoc-openapi library scans all @RestController classes
 * and reads annotations (@Operation, @ApiResponse, etc.) to generate docs.
 * This @Configuration class adds metadata like the API title, version, contact.
 *
 * @Configuration — Marks this as a Spring configuration class.
 *                  Methods annotated with @Bean become Spring-managed beans.
 * @Bean — Spring calls this method once and stores the result in the Application Context.
 */
@Configuration
public class OpenApiConfig {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Defines the OpenAPI metadata shown at the top of the Swagger UI page.
     * Also adds the server URL so Swagger can send test requests directly.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Server configuration — used for "Try it out" requests in Swagger UI
                .servers(List.of(
                    new Server()
                        .url(baseUrl)
                        .description("Current Environment")
                ))
                // API metadata shown in Swagger UI header
                .info(new Info()
                    .title("URL Shortener API")
                    .version("1.0.0")
                    .description("""
                        A production-ready URL Shortener REST API built with Spring Boot.
                        
                        Features:
                        - Create short URLs from long URLs
                        - Custom short code support
                        - Click tracking and analytics
                        - URL expiration support
                        - Soft delete / deactivation
                        """)
                    .contact(new Contact()
                        .name("Your Name")
                        .email("your.email@example.com")
                        .url("https://github.com/yourusername/url-shortener")
                    )
                    .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                    )
                );
    }
}
