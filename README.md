# Resume Builder — Spring Boot REST API

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-7-47A248?logo=mongodb&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

A JWT-secured REST API for creating, editing, and managing resumes with template switching. Built with **Java 17**, **Spring Boot 3**, **Spring Security**, and **Spring Data MongoDB**, following a layered **Controller → Service → Repository** architecture.

---

## Features

- **JWT authentication.** Register and log in to receive a signed token. Passwords are hashed with BCrypt, and every request is checked by a custom `OncePerRequestFilter`.
- **Role-based access control.** Users get `USER` by default; routes under `/api/admin/**` require `ADMIN`.
- **Owner-scoped data.** Every resume query is filtered by the authenticated user's ID, so users can never read or modify each other's resumes.
- **Pagination, filtering, and sorting** on the resume list endpoint, with safe defaults and a page-size cap of 50.
- **Template switching.** Change a resume's template (`classic`, `modern`, `compact`) without touching its content.
- **Flexible document model.** Resume sections (Experience, Education, Projects, Skills, …) are stored as ordered generic blocks, a natural fit for MongoDB.
- **Consistent error responses.** All errors return the same JSON shape via `@RestControllerAdvice`.
- **Dockerized.** A multi-stage Docker build plus `docker-compose` runs the API and MongoDB together with one command.

## Tech Stack

| Layer          | Technology                                   |
|----------------|----------------------------------------------|
| Language       | Java 17                                      |
| Framework      | Spring Boot 3.3 (Web, Validation)            |
| Security       | Spring Security, JJWT 0.11.5, BCrypt         |
| Database       | MongoDB 7, Spring Data MongoDB               |
| Build          | Maven                                        |
| Infrastructure | Docker (multi-stage build), Docker Compose   |
| Utilities      | Lombok                                       |

## Project Structure

```
Resume-Builder/
└── rb/
    ├── src/main/java/com/adarsh/resumebuilder/
    │   ├── controller/   # Thin HTTP layer: Auth, Resume, Admin
    │   ├── service/      # Business rules, ownership checks, validation
    │   ├── repository/   # Spring Data MongoDB interfaces
    │   ├── model/        # User and Resume documents
    │   ├── dto/          # Auth request/response records, PageResponse
    │   ├── security/     # JwtService, JwtAuthFilter, UserDetailsService
    │   └── config/       # SecurityConfig (RBAC), MongoConfig, error handling
    ├── src/main/resources/application.yml
    ├── Dockerfile
    ├── docker-compose.yml
    └── pom.xml
```

## Getting Started

### Option 1: Docker (recommended)

Requires Docker and Docker Compose.

```bash
git clone https://github.com/Aadarsh-07/Resume-Builder.git
cd Resume-Builder/rb
docker compose up --build
```

The API starts at `http://localhost:8080`, with MongoDB at `localhost:27017`.

### Option 2: Run locally

Requires Java 17+, Maven, and MongoDB running on port `27017`.

```bash
git clone https://github.com/Aadarsh-07/Resume-Builder.git
cd Resume-Builder/rb
mvn spring-boot:run
```

### Configuration

All settings can be overridden with environment variables:

| Variable            | Default                                        | Description                         |
|---------------------|------------------------------------------------|-------------------------------------|
| `MONGODB_URI`       | `mongodb://localhost:27017/resume_builder`     | MongoDB connection string           |
| `JWT_SECRET`        | development placeholder                        | Signing key, at least 64 characters |
| `JWT_EXPIRATION_MS` | `86400000` (24 hours)                          | Token lifetime                      |
| `PORT`              | `8080`                                         | Server port                         |

> **Note:** Always set your own `JWT_SECRET` outside local development.

## API Reference

| Method | Endpoint                      | Auth   | Description                                  |
|--------|-------------------------------|--------|----------------------------------------------|
| POST   | `/api/auth/register`          | Public | Create an account and receive a JWT          |
| POST   | `/api/auth/login`             | Public | Log in and receive a JWT                     |
| GET    | `/api/resumes`                | JWT    | List your resumes (paginated, filterable)    |
| POST   | `/api/resumes`                | JWT    | Create a resume                              |
| GET    | `/api/resumes/{id}`           | JWT    | Get one of your resumes                      |
| PUT    | `/api/resumes/{id}`           | JWT    | Update a resume                              |
| PATCH  | `/api/resumes/{id}/template`  | JWT    | Switch template (`classic`/`modern`/`compact`) |
| DELETE | `/api/resumes/{id}`           | JWT    | Delete a resume                              |
| GET    | `/api/admin/stats`            | Admin  | Total user and resume counts                 |

### List query parameters

| Parameter   | Default     | Notes                                       |
|-------------|-------------|---------------------------------------------|
| `page`      | `0`         | Zero-based page index                       |
| `size`      | `10`        | Capped at 50                                |
| `sortBy`    | `updatedAt` | One of `updatedAt`, `createdAt`, `title`    |
| `direction` | `desc`      | `asc` or `desc`                             |
| `title`     | none        | Case-insensitive "contains" filter          |

## Usage Example

```bash
# 1. Register and capture the token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Adarsh","email":"adarsh@example.com","password":"secret123"}' | jq -r .token)

# 2. Create a resume
curl -s -X POST http://localhost:8080/api/resumes \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
        "title": "Backend Engineer Resume",
        "template": "modern",
        "personalInfo": { "fullName": "Adarsh Kumar Seth", "email": "adarsh@example.com" },
        "sections": [
          { "heading": "Experience", "order": 1, "items": [ { "role": "SDE-1", "company": "Skeps" } ] }
        ]
      }'

# 3. List resumes, newest first, filtered by title
curl -s "http://localhost:8080/api/resumes?page=0&size=5&sortBy=updatedAt&direction=desc&title=backend" \
  -H "Authorization: Bearer $TOKEN"

# 4. Switch the template
curl -s -X PATCH http://localhost:8080/api/resumes/<RESUME_ID>/template \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"template":"compact"}'
```

### Paginated response

```json
{
  "content": [ { "id": "...", "title": "Backend Engineer Resume", "template": "modern" } ],
  "page": 0,
  "size": 5,
  "totalElements": 1,
  "totalPages": 1,
  "hasNext": false
}
```

### Error response

```json
{
  "timestamp": "2026-10-11T10:15:30Z",
  "status": 400,
  "message": "Unknown template 'fancy'. Valid: [classic, modern, compact]"
}
```

## Architecture

```
Client ──► JwtAuthFilter ──► Controller ──► Service ──► Repository ──► MongoDB
             (validates        (HTTP         (business     (Spring Data
              token, sets       mapping)      rules,        queries)
              security                        ownership,
              context)                        validation)
```

- **Controllers** only map HTTP requests and responses.
- **Services** hold all business logic, including ownership checks and template validation.
- **Repositories** are plain Spring Data interfaces with derived queries.

## Author

**Adarsh Kumar Seth**
[LinkedIn](https://www.linkedin.com/in/adarsh-kumar-seth) · [GitHub](https://github.com/Aadarsh-07)
