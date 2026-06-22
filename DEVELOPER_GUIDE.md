# 📘 Complete Developer Guide — URL Shortener
## Step-by-Step Build, Test, and Deploy Instructions

---

## 🗺️ Table of Contents

1. [Understanding the Project Structure](#1-understanding-the-project-structure)
2. [Milestone 1 — Database Setup](#2-milestone-1--database-setup)
3. [Milestone 2 — Running the App Locally](#3-milestone-2--running-the-app-locally)
4. [Milestone 3 — Testing the API](#4-milestone-3--testing-the-api)
5. [Milestone 4 — Running Unit Tests](#5-milestone-4--running-unit-tests)
6. [Milestone 5 — Building the JAR](#6-milestone-5--building-the-jar)
7. [Milestone 6 — Deploying to the Cloud](#7-milestone-6--deploying-to-the-cloud)
8. [Git Commit Messages](#8-git-commit-messages)
9. [Resume Bullet Points](#9-resume-bullet-points)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Understanding the Project Structure

Before you build anything, understand what each file does:

```
url-shortener/
│
│  ← ENTRY POINT
├── UrlShortenerApplication.java    Starts the app (main method)
│
│  ← DATABASE LAYER
├── entity/UrlMapping.java          Java class = MySQL table row
├── repository/UrlMapping           Database queries (Spring handles SQL)
│
│  ← BUSINESS LOGIC
├── service/UrlShortenerService.java All the "rules" live here
├── util/ShortCodeGenerator.java    Generates random codes like "a8bX2k"
│
│  ← HTTP LAYER
├── controller/
│   ├── UrlShortenerController.java REST API (/api/urls)
│   ├── RedirectController.java     Handles /{code} redirects
│   └── WebController.java          Serves HTML pages
│
│  ← DATA SHAPES
├── dto/
│   ├── CreateUrlRequest.java       What the client SENDS us
│   ├── UrlResponse.java            What we SEND BACK
│   └── ApiError.java              What errors look like
│
│  ← ERROR HANDLING
├── exception/
│   ├── GlobalExceptionHandler.java Catches ALL exceptions app-wide
│   ├── UrlNotFoundException.java   404 exception
│   ├── ShortCodeAlreadyExistsException.java  409 exception
│   └── UrlExpiredException.java    410 exception
│
│  ← CONFIGURATION
├── config/OpenApiConfig.java       Swagger documentation setup
├── resources/application.properties  Database URL, passwords, etc.
│
│  ← FRONTEND TEMPLATES
├── resources/templates/
│   ├── index.html     Home page (URL shortening form)
│   ├── result.html    Success page (shows the short URL)
│   └── dashboard.html Statistics page (all URLs listed)
│
│  ← TESTS
└── test/.../UrlShortenerServiceTest.java  Unit tests
```

### How a Request Flows Through the App

**Example: User creates a short URL**

```
1. Browser/Client sends:
   POST /api/urls
   Body: { "originalUrl": "https://very-long-url.com" }

2. Spring routes to UrlShortenerController.createShortUrl()
   → @Valid validates the request body

3. Controller calls urlShortenerService.createShortUrl(request)

4. Service:
   a. Checks if customCode given (no → generate one)
   b. Calls shortCodeGenerator.generate() → "a8bX2k"
   c. Checks repository.existsByShortCode("a8bX2k") → false (unique!)
   d. Builds UrlMapping entity
   e. Calls repository.save(mapping) → INSERT into MySQL
   f. Converts entity → UrlResponse DTO
   g. Returns UrlResponse

5. Controller wraps in ResponseEntity(201 CREATED)

6. Spring converts UrlResponse → JSON
   Returns:
   {
     "shortCode": "a8bX2k",
     "shortUrl": "http://localhost:8080/a8bX2k",
     "clickCount": 0,
     ...
   }
```

**Example: User visits the short URL**

```
1. Browser requests: GET /a8bX2k

2. Spring routes to RedirectController.redirect("a8bX2k")

3. Controller calls urlShortenerService.resolveUrl("a8bX2k")

4. Service:
   a. Calls repository.findActiveByShortCode("a8bX2k")
   b. Checks if expired (no) 
   c. Calls repository.incrementClickCount(id) [atomic DB update]
   d. Returns "https://very-long-url.com"

5. Controller sets Location header = "https://very-long-url.com"
   Returns: 302 Found (redirect)

6. Browser follows the Location header → navigates to original URL
```

---

## 2. Milestone 1 — Database Setup

### Step 1: Install MySQL (if not installed)

**Windows:**
1. Download MySQL Installer: https://dev.mysql.com/downloads/installer/
2. Choose "MySQL Server" + "MySQL Workbench"
3. Set root password (remember it!)

**Mac:**
```bash
brew install mysql
brew services start mysql
mysql_secure_installation    # Set root password
```

**Linux (Ubuntu):**
```bash
sudo apt update
sudo apt install mysql-server
sudo mysql_secure_installation
```

### Step 2: Create the Database

Open your terminal and log into MySQL:
```bash
mysql -u root -p
# Enter your root password when prompted
```

You should see the `mysql>` prompt. Now create the database:
```sql
CREATE DATABASE urlshortener CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Verify it was created:
```sql
SHOW DATABASES;
```

You should see `urlshortener` in the list.

Exit MySQL:
```sql
EXIT;
```

### Step 3: Run the Schema File

From your project folder:
```bash
mysql -u root -p urlshortener < schema.sql
```

Verify the table was created:
```bash
mysql -u root -p urlshortener -e "SHOW TABLES; DESCRIBE url_mappings;"
```

You should see:
```
+--------------------+
| Tables_in_urlshortener |
+--------------------+
| url_mappings       |
+--------------------+
```

### ✅ Milestone 1 Checkpoint

Run this command to confirm everything is ready:
```sql
mysql -u root -p urlshortener -e "SELECT COUNT(*) FROM url_mappings;"
-- Expected output: 0 (empty table, ready for data)
```

---

## 3. Milestone 2 — Running the App Locally

### Step 1: Configure Database Credentials

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/urlshortener?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_ACTUAL_PASSWORD_HERE
```

⚠️ **Replace `YOUR_ACTUAL_PASSWORD_HERE`** with the MySQL root password you set during installation.

### Step 2: Install Java 21

Check if you have Java 21:
```bash
java -version
# Should show: openjdk 21.x.x
```

If not, download from: https://adoptium.net/
- Choose: JDK 21, Latest Release

### Step 3: Install Maven (or use Maven Wrapper)

Check if Maven is installed:
```bash
mvn -version
# Should show: Apache Maven 3.x.x
```

If not installed, you can use the Maven Wrapper that comes with Spring Boot projects:
```bash
# On Mac/Linux, use ./mvnw instead of mvn
./mvnw spring-boot:run

# On Windows, use mvnw.cmd
mvnw.cmd spring-boot:run
```

### Step 4: Run the Application

```bash
cd url-shortener
mvn spring-boot:run
```

You should see output ending with:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

... Started UrlShortenerApplication in 3.456 seconds
```

### ✅ Milestone 2 Checkpoint

Open these URLs in your browser:

| URL | Expected Result |
|-----|----------------|
| http://localhost:8080 | Home page with URL shortening form |
| http://localhost:8080/dashboard | Dashboard (empty at first) |
| http://localhost:8080/swagger-ui.html | Interactive API documentation |
| http://localhost:8080/actuator/health | `{"status":"UP"}` |

Try shortening a URL through the web form:
1. Go to http://localhost:8080
2. Enter `https://www.google.com` in the field
3. Click "Shorten URL"
4. You should see a result page with your short URL
5. Click the short URL — it should redirect to Google

**Check the database to confirm the record was saved:**
```sql
mysql -u root -p urlshortener -e "SELECT id, short_code, original_url, click_count FROM url_mappings;"
```

---

## 4. Milestone 3 — Testing the API

### Option A: Using Swagger UI (Easiest)

1. Open http://localhost:8080/swagger-ui.html
2. Click on "URL Shortener API" to expand
3. Click "POST /api/urls" → "Try it out"
4. Paste this request body:
```json
{
  "originalUrl": "https://www.github.com"
}
```
5. Click "Execute"
6. You should see a 201 response with the short URL

### Option B: Using Postman

1. Open Postman
2. Click **Import** → **File** → select `LinkSnip-API.postman_collection.json`
3. In the collection, click the three dots → **Edit** → **Variables**
4. Confirm `baseUrl` is set to `http://localhost:8080`
5. Run requests 1-11 in order

### Option C: Using curl (Command Line)

**Create a short URL:**
```bash
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://www.google.com"}' \
  | python3 -m json.tool
```

**Expected response:**
```json
{
  "id": 1,
  "originalUrl": "https://www.google.com",
  "shortCode": "a8bX2k",
  "shortUrl": "http://localhost:8080/a8bX2k",
  "clickCount": 0,
  "createdAt": "2024-01-15T10:30:00",
  "isActive": true
}
```

**Create with custom code:**
```bash
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://spring.io","customCode":"spring","title":"Spring Website"}'
```

**Get all URLs:**
```bash
curl http://localhost:8080/api/urls | python3 -m json.tool
```

**Test redirect (the -L flag follows redirects):**
```bash
curl -L http://localhost:8080/a8bX2k
# Should redirect to https://www.google.com
```

**Test without following redirect (see the 302):**
```bash
curl -v http://localhost:8080/a8bX2k 2>&1 | grep "Location:"
# Expected: < Location: https://www.google.com
```

**Test 404:**
```bash
curl -v http://localhost:8080/api/urls/doesnotexist
# Expected: 404 response with JSON error
```

**Test validation error:**
```bash
curl -X POST http://localhost:8080/api/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "not-a-url"}'
# Expected: 400 with fieldErrors
```

**Delete (deactivate) a URL:**
```bash
curl -X DELETE http://localhost:8080/api/urls/a8bX2k
# Expected: 204 No Content
```

### ✅ Milestone 3 Checkpoint

After testing, verify in the database that click counts updated:
```sql
mysql -u root -p urlshortener -e "SELECT short_code, click_count, is_active FROM url_mappings;"
```

---

## 5. Milestone 4 — Running Unit Tests

Unit tests verify your business logic without needing a running server or real database.

### Run All Tests
```bash
mvn test
```

### Expected Output
```
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0

[INFO] BUILD SUCCESS
```

### View Test Report
After running tests, open:
```
target/surefire-reports/
```

Or view in your IDE:
- IntelliJ: Click the green play button next to test class names
- VS Code: Install "Test Runner for Java" extension

### ✅ Milestone 4 Checkpoint

All 11 tests should pass. If any fail:
1. Read the error message carefully
2. Check that your code matches the provided files exactly
3. Common issues: missing Lombok annotation processor in IDE

---

## 6. Milestone 5 — Building the JAR

A JAR (Java ARchive) is a single executable file containing your entire application + all dependencies. This is what you deploy to a server.

### Build the JAR
```bash
mvn clean package
```

- `clean` — deletes the `target/` folder (removes old build artifacts)
- `package` — compiles + tests + packages into a JAR

Skip tests if you want a faster build (not recommended for production):
```bash
mvn clean package -DskipTests
```

### Find the JAR
```bash
ls target/*.jar
# Should show: target/url-shortener-1.0.0.jar
```

### Run the JAR
```bash
java -jar target/url-shortener-1.0.0.jar
```

With environment variables:
```bash
DB_URL="jdbc:mysql://localhost:3306/urlshortener?useSSL=false&serverTimezone=UTC" \
DB_USERNAME="root" \
DB_PASSWORD="yourpassword" \
java -jar target/url-shortener-1.0.0.jar
```

### ✅ Milestone 5 Checkpoint

The JAR should start and respond to http://localhost:8080 just like `mvn spring-boot:run` did.

---

## 7. Milestone 6 — Deploying to the Cloud

### Option A: Deploy on Render (Recommended — Free)

#### Step 1: Push to GitHub

```bash
# Initialize git (if not already done)
cd url-shortener
git init
git add .
git commit -m "feat: initial URL shortener implementation"

# Create repo on github.com then:
git remote add origin https://github.com/YOURUSERNAME/url-shortener.git
git branch -M main
git push -u origin main
```

#### Step 2: Create a Free Cloud MySQL Database

**Using PlanetScale (free, no credit card):**
1. Go to https://planetscale.com → Sign Up (use GitHub)
2. Create New Database → Name: `urlshortener` → Region: closest to you
3. Go to **Connect** → Connect With: **Java / JDBC**
4. Copy the connection string — looks like:
   ```
   jdbc:mysql://aws.connect.psdb.cloud/urlshortener?sslMode=VERIFY_IDENTITY&...
   ```
5. Click "Create Password" → Copy username and password (shown only once!)

**Note:** PlanetScale doesn't use DDL (CREATE TABLE) at the app layer the same way.  
Instead: go to Branches → main → Console → paste the CREATE TABLE from `schema.sql`.

**Alternative — Railway (slightly easier but requires credit card for verification):**
1. Go to https://railway.app → New Project → Database → MySQL
2. Click MySQL → Variables tab → Copy `MYSQL_URL`

#### Step 3: Deploy on Render

1. Go to https://render.com → Sign up with GitHub
2. **New** → **Web Service**
3. Connect your GitHub repo
4. Configure:
   - **Name:** `linksnip-url-shortener`
   - **Branch:** `main`
   - **Build Command:** `mvn clean package -DskipTests`
   - **Start Command:** `java -jar target/url-shortener-1.0.0.jar`
   - **Plan:** Free
5. Click **Advanced** → **Add Environment Variable**:
   ```
   DB_URL        = [your PlanetScale/Railway JDBC URL]
   DB_USERNAME   = [your DB username]
   DB_PASSWORD   = [your DB password]
   APP_BASE_URL  = https://linksnip-url-shortener.onrender.com
   ```
6. Click **Create Web Service**
7. Wait 3-5 minutes for the first build

#### Step 4: Get Your Public URL

After deployment succeeds, Render shows your URL:
```
https://linksnip-url-shortener.onrender.com
```

Update `APP_BASE_URL` in Render → Environment → edit the variable.

#### Step 5: Verify Deployment

```bash
# Health check
curl https://linksnip-url-shortener.onrender.com/actuator/health

# Create a short URL
curl -X POST https://linksnip-url-shortener.onrender.com/api/urls \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://www.google.com"}'

# Visit in browser
# https://linksnip-url-shortener.onrender.com
```

⚠️ **Free Render Note:** The free tier "spins down" after 15 minutes of inactivity.
The first request after idle will take ~30 seconds to wake up. This is normal for free hosting.

### ✅ Milestone 6 Checkpoint

Your app is live! Share the URL and test:
1. Open your Render URL in a browser
2. Shorten a URL through the web form
3. Click the short URL — confirm it redirects
4. Check the dashboard

---

## 8. Git Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/) — industry standard format:

```
<type>(<scope>): <short description>
```

Types: `feat` (new feature), `fix` (bug fix), `docs`, `test`, `refactor`, `chore`

**Suggested commits for this project:**

```bash
# Project setup
git commit -m "chore: initialize Spring Boot project with Maven"
git commit -m "chore: add dependencies (JPA, MySQL, Lombok, Swagger, Validation)"

# Database layer
git commit -m "feat(entity): add UrlMapping JPA entity with all fields"
git commit -m "feat(repository): add UrlMappingRepository with custom queries"

# Business logic
git commit -m "feat(util): add Base62 short code generator (ShortCodeGenerator)"
git commit -m "feat(service): implement URL creation and resolution logic"
git commit -m "feat(service): add click count tracking with atomic DB update"

# HTTP layer
git commit -m "feat(controller): add REST API endpoints (POST, GET, DELETE)"
git commit -m "feat(controller): add redirect controller for short URL resolution"
git commit -m "feat(controller): add Thymeleaf web UI controller"

# Error handling
git commit -m "feat(exception): add custom exceptions (404, 409, 410)"
git commit -m "feat(exception): add global exception handler with consistent JSON errors"

# Frontend
git commit -m "feat(ui): add Thymeleaf templates (index, result, dashboard)"
git commit -m "feat(ui): add CSS styling and JavaScript for web interface"

# Tests
git commit -m "test(service): add unit tests for UrlShortenerService (11 tests)"
git commit -m "test(util): add unit tests for ShortCodeGenerator"

# Configuration
git commit -m "feat(config): add Swagger/OpenAPI documentation configuration"
git commit -m "docs: add comprehensive README with setup and deployment guide"
git commit -m "docs: add Postman collection for API testing"
git commit -m "chore: add .gitignore and render.yaml for deployment"
```

---

## 9. Resume Bullet Points

Use these on your software engineering internship resume under **Projects**:

---

**LinkSnip — URL Shortener** | Java, Spring Boot, MySQL, REST API | [GitHub Link]

- **Built a production-ready URL shortening service** using Java 21 and Spring Boot 3, implementing a full layered architecture (Controller → Service → Repository → Entity) with clean separation of concerns, Swagger/OpenAPI documentation, and a Thymeleaf web UI — deployed on Render with a cloud MySQL database

- **Designed and implemented a RESTful API** with 5 endpoints handling URL creation (custom + auto-generated Base62 codes), redirection with atomic click-count tracking, and soft-delete; returning standardized JSON error responses (400/404/409/410/500) via a centralized GlobalExceptionHandler using @RestControllerAdvice

- **Wrote 11 unit tests** using JUnit 5 and Mockito with an H2 in-memory database, achieving isolated testing of all service-layer business rules including collision-resistant code generation, URL expiration logic, and concurrent click-count tracking with direct atomic UPDATE queries

- **Implemented production-grade features** including SecureRandom Base62 short code generation (62⁶ = ~56 billion combinations), URL expiration with HTTP 410 Gone responses, environment-variable-based configuration for zero-secret commits, SLF4J structured logging, and Spring Boot Actuator health endpoints for deployment monitoring

---

## 10. Troubleshooting

### "Access denied for user 'root'" (MySQL auth error)
```bash
# Check your MySQL username and password
mysql -u root -p

# If you forgot the password, reset it:
# (Mac/Linux) sudo mysql
# ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'newpassword';
# FLUSH PRIVILEGES;
```

### "Port 8080 already in use"
```bash
# Find and kill what's using port 8080
lsof -i :8080             # Mac/Linux
netstat -ano | findstr 8080  # Windows

# Or change the port in application.properties:
server.port=8081
```

### "Cannot find symbol: @Getter / @Setter" (Lombok not working)
- **IntelliJ:** Settings → Plugins → Install "Lombok" → Enable annotation processing
- Settings → Build → Compiler → Annotation Processors → ✅ Enable

### "Table 'urlshortener.url_mappings' doesn't exist"
```bash
# Re-run the schema file
mysql -u root -p urlshortener < schema.sql

# Or let Spring create it (set in application.properties):
spring.jpa.hibernate.ddl-auto=create
# (Run once, then change back to "update")
```

### Redirect loop or wrong short URL in response
- Check `app.base-url` in `application.properties`
- For local: `app.base-url=http://localhost:8080`
- For production: `app.base-url=https://your-domain.com`

### App works locally but fails on Render
1. Check Render logs (Dashboard → your service → Logs)
2. Verify all 3 env vars are set: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
3. Test the DB connection string separately using a MySQL client
4. Ensure cloud DB allows connections from Render's IPs (check PlanetScale/Railway settings)

### Tests failing: "Could not connect to H2 database"
- The test configuration is in `application-test.properties`
- Check `src/test` folder has the same package structure as `src/main`
- Add `@ActiveProfiles("test")` to test classes if needed
