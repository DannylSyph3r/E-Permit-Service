# EPermit Service

A multi-tenant permit issuance microservice for the DIGIT platform. Built with Spring Boot 4,
PostgreSQL, and RabbitMQ.

---

## Running the Project

The only requirement is Docker. Clone the repository and run this single command from the project
root:

```bash
docker compose up --build
```

This starts three containers: the PostgreSQL database, RabbitMQ, and the application itself. The
application waits for both the database and the message broker to be healthy before starting. Flyway
runs the database migrations automatically on startup, including seeding two test users. The API is
available at `http://localhost:8080` once all three containers are running.

To stop and remove all containers and volumes:

```bash
docker compose down -v
```

---

## Test Credentials

Two users are seeded automatically. No registration is required to start testing.

| Tenant | Email | Password |
|---|---|---|
| Ministry_Health | health@test.com | test1234 |
| Ministry_Education | education@test.com | test1234 |

---

## API Docs

Interactive Swagger UI is available at `http://localhost:8080/docs` once the service is running.
All endpoints are documented with request and response schemas.

---

## Quick Start Flow

**1. Get a token**
```
POST /api/auth/token
{ "email": "health@test.com", "password": "test1234" }
```

**2. Submit a permit application**
```
POST /api/permits
Headers: Authorization: Bearer <token>
         X-Tenant-ID: Ministry_Health
```

**3. Retrieve all permits for the tenant**
```
GET /api/permits/summary
Headers: Authorization: Bearer <token>
         X-Tenant-ID: Ministry_Health
```

---

## Project Structure

All application code lives under `src/main/java/dev/slethware/epermitservice/`.

| Package | Responsibility |
|---|---|
| `config` | Spring beans for JWT, RabbitMQ, datasource, WebClient, and password encoding |
| `controller` | HTTP layer for authentication, permit operations, and the internal payment test stub |
| `exception` | Custom exception classes and the global exception handler |
| `model/entity` | JPA entities: `User`, `Permit`, and `PermitDocument` |
| `model/dto` | Request and response records used at the HTTP boundary |
| `model/enums` | `PermitStatus`, `PaymentStatus`, and `PermitType` |
| `repository` | Spring Data JPA repositories, including the JOIN FETCH query for permit retrieval |
| `security` | JWT filter, tenant validation filter, tenant context holder, and security configuration |
| `service/auth` | User registration and login logic |
| `service/token` | JWT generation and validation |
| `service/permit` | Core permit creation and retrieval logic |
| `service/payment` | Resilience4j-wrapped payment call and fallback handling |
| `service/event` | Publishes `PermitCreatedEvent` to RabbitMQ after a permit is saved |

Database migrations live under `src/main/resources/db/migration/`. Flyway runs these in order on
every startup. `V1` creates the schema and applies Row-Level Security. `V2` seeds the test users.