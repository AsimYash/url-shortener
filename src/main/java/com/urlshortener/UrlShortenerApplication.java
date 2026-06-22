package com.urlshortener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * UrlShortenerApplication — Entry Point
 *
 * @SpringBootApplication is a shortcut for three annotations:
 *   1. @Configuration       — This class can define Spring beans
 *   2. @EnableAutoConfiguration — Spring Boot auto-configures based on classpath
 *      (e.g., sees MySQL driver → sets up DataSource automatically)
 *   3. @ComponentScan       — Scans this package and sub-packages for
 *      @Service, @Repository, @Controller, @Component beans
 *
 * The main() method is a standard Java entry point.
 * SpringApplication.run() bootstraps the entire application:
 *   - Starts the embedded Tomcat server (default port 8080)
 *   - Initializes the Spring Application Context
 *   - Connects to the database and runs schema setup
 */
@SpringBootApplication
public class UrlShortenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);
    }
}
