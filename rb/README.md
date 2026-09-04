# Resume Builder — Spring Boot REST API

Backend for a full-stack resume builder: JWT-secured REST API for creating,
editing, and managing resumes with live template switching. Built with
**Java 17, Spring Boot 3, Spring Security, and Spring Data MongoDB**, following
a layered **Controller → Service → Repository** architecture.

## Features
- **JWT authentication** (register/login) with BCrypt password hashing, via a
  custom `OncePerRequestFilter` and Spring Security
- **Role-based access control** — `/api/admin/**` requires `ROLE_ADMIN`
- **Resume CRUD** with **pagination, filtering, and sorting**
  (`?page=0&size=10&sortBy=updatedAt&direction=desc&title=backend`)
- **Template switching** endpoint (`PATCH /api/resumes/{id}/template`)
- Flexible MongoDB document model (`sections` as ordered generic blocks)
- Consistent JSON error envelope via `@RestControllerAdvice`
- Dockerized: multi-stage build + `docker-compose` with MongoDB

## Quick start
```bash
# with Docker (recommended)
docker compose up --build

# or locally (needs Java 17+, Maven, and MongoDB on :27017)
mvn spring-boot:run
```

## API overview
| Method | Endpoint                          | Auth   | Description                              |
|--------|-----------------------------------|--------|------------------------------------------|
| POST   | /api/auth/register                | public | Create account, returns JWT              |
| POST   | /api/auth/login                   | public | Login, returns JWT                       |
| GET    | /api/resumes                      | JWT    | List own resumes (paginated/filter/sort) |
| POST   | /api/resumes                      | JWT    | Create resume                            |
| GET    | /api/resumes/{id}                 | JWT    | Get one (owner-scoped)                   |
| PUT    | /api/resumes/{id}                 | JWT    | Update resume                            |
| PATCH  | /api/resumes/{id}/template        | JWT    | Switch template (classic/modern/compact) |
| DELETE | /api/resumes/{id}                 | JWT    | Delete resume                            |
| GET    | /api/admin/stats                  | ADMIN  | User/resume counts (RBAC demo)           |

## Example
```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"name":"Adarsh","email":"a@b.com","password":"secret123"}' | jq -r .token)

curl -s localhost:8080/api/resumes -H "Authorization: Bearer $TOKEN"
```

## Architecture
```
controller/   thin HTTP layer, request/response mapping
service/      business rules, ownership checks, validation
repository/   Spring Data MongoDB interfaces
security/     JwtService, JwtAuthFilter, UserDetailsService
config/       SecurityConfig (RBAC), error handling, auditing
```
