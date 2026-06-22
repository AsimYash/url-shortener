# 🔗 LinkSnip — URL Shortener

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-green)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)
![Maven](https://img.shields.io/badge/Maven-Build-red)

A full-stack URL Shortener web application built using **Java 21, Spring Boot, Spring Data JPA, MySQL, and Thymeleaf**.

LinkSnip is a full-stack URL shortening platform that supports custom short codes, automatic URL generation, input validation, persistent storage, and browser-based redirection through a clean web interface.

---

## 📦 Repository

[GitHub Repository](https://github.com/AsimYash/url-shortener)

## 🌐 Live Demo

Coming Soon (Render Deployment)

---

## 🚀 Features

* 🔗 Create short URLs from long URLs
* 🎯 Custom short codes
* ⚡ Automatic short code generation
* 🔄 Redirect users to the original URL
* ✅ URL validation
* 🛡️ Exception handling
* 💾 Persistent storage using MySQL
* 🌐 Web-based user interface

---

## 🛠️ Tech Stack

### Backend

* Java 21
* Spring Boot 3
* Spring MVC
* Spring Data JPA
* Hibernate

### Database

* MySQL

### Frontend

* Thymeleaf
* HTML
* CSS
* JavaScript

### Build Tool

* Maven

---

## 📂 Project Structure

```text
url-shortener/
│
├── src/
│   ├── main/
│   │   ├── java/com/urlshortener/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   └── dto/
│   │   │
│   │   └── resources/
│   │       ├── templates/
│   │       ├── static/
│   │       └── application.properties
│
├── screenshots/
├── pom.xml
└── README.md
```

---

## ⚙️ Running Locally

### Prerequisites

Install:

* Java 21+
* Maven
* MySQL

Check versions:

```bash
java -version
mvn -version
```

### Clone Repository

```bash
git clone https://github.com/AsimYash/url-shortener.git
cd url-shortener
```

### Create MySQL Database

```sql
CREATE DATABASE urlshortener;
```

### Configure Database

Open:

```text
src/main/resources/application.properties
```

Update:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/urlshortener
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

Replace `YOUR_PASSWORD` with your MySQL password.

### Run Application

Using Maven:

```bash
mvn spring-boot:run
```

Or build and run:

```bash
mvn clean package
java -jar target/*.jar
```

---

## 🌐 Open Application

Open:

```text
http://localhost:8080
```

---

## 🧪 Testing

### Create Short URL

Long URL:

```text
https://www.google.com
```

Custom Code:

```text
google
```

Generated URL:

```text
http://localhost:8080/google
```

Opening the short URL redirects to:

```text
https://www.google.com
```

### Validation Example

Invalid input:

```text
google.com
```

Result:

```text
URL must start with http:// or https://
```

---

## 📸 Screenshots

### Home Page

![Home](screenshots/Home.png)

### URL Creation

![Creation](screenshots/Creation.png)

### Successful URL Creation

![Success](screenshots/Success.png)

### Redirect Working

![Redirect](screenshots/Redirect.png)

### Validation Error

![Validation](screenshots/Validation.png)

---

## 🔮 Future Improvements

* User Authentication
* JWT Security
* QR Code Generation
* Click Analytics Dashboard
* Rate Limiting
* Custom Domains
* Docker Deployment
* Cloud Hosting

---

## 📌 API Endpoints

### Create Short URL

```http
POST /api/urls
```

Example Request:

```json
{
  "originalUrl": "https://example.com",
  "customCode": "example"
}
```

### Redirect

```http
GET /{shortCode}
```

Example:

```http
GET /example
```

Redirects to the original URL.

---

## 👨‍💻 Author

**Asim Yash**

GitHub: https://github.com/AsimYash
