# spring-gym

A **Spring Boot REST API** for gym management (CRM) with
JWT authentication, OpenAPI documentation, and Prometheus/Grafana monitoring.

## Tech Stack

- **Spring Boot 4.0.2** (Web MVC, Data JPA, Validation, Actuator)
- **Spring Security** + **BCrypt** password hashing
- **MySQL 9.x** + **Flyway** migrations
- **JWT** authentication (jjwt 0.13.0)
- **OpenAPI 3.0** + Swagger UI
- **Micrometer** + Prometheus + Grafana
- **Testcontainers** + JaCoCo (80% coverage enforced, with exclusions)
- **Java 21**

## Quick Start

### 1. Start Infrastructure

```bash
cd docker
docker-compose up -d
```

### 2. Build & Run

#### 2.a Run directly on host

```bash
export SPRING_PROFILES_ACTIVE=local
./mvnw clean package
java -jar target/spring-gym-1.1-SNAPSHOT.jar --spring.profiles.active=local
```

#### 2.b Run in a docker container

```bash
docker run \
  -p 8080:8080 -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=docker \
  --network gymapp-network \
  iasw/spring-gym:latest
```

### 3. Access

| Resource   | URL                                   |
|------------|---------------------------------------|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Base   | http://localhost:8080/api/v1          |
| Health     | http://localhost:8080/actuator/health |
| Grafana    | http://localhost:3000 (admin/admin)   |

## API Endpoints

### Public (No Auth)

| Method | Endpoint                 | Description         |
|--------|--------------------------|---------------------|
| POST   | `/api/v1/trainees`       | Register trainee    |
| POST   | `/api/v1/trainers`       | Register trainer    |
| POST   | `/api/v1/auth/login`     | Get JWT token       |
| GET    | `/api/v1/training-types` | List training types |

### Protected (Bearer Token)

- **Auth**: `POST /api/v1/auth/logout`
- **Trainees**: `GET/PUT/DELETE /{username}`, password, status, trainers, trainings
- **Trainers**: `GET/PUT /{username}`, password, status, trainings
- **Trainings**: `POST /api/v1/trainings`

> **Note**: Users can only access their own resources (enforced via `@SelfService` AOP annotation).

### Auth Flow

```
1. POST /api/v1/trainees → { username, password }
2. POST /api/v1/auth/login → { token, role }
3. Use header: Authorization: Bearer <token>
```

## Security Features

| Feature                        | Description                                           |
|--------------------------------|-------------------------------------------------------|
| **BCrypt Hashing**             | Passwords hashed with BCrypt encoder                  |
| **JWT Tokens**                 | 24-hour expiration, blacklisted on logout             |
| **Brute Force Protection**     | 3 failed attempts → 5 min lockout (429 + Retry-After) |
| **Token Blacklist**            | Logged-out tokens rejected until expiration           |
| **Self-Service Authorization** | Users can only access their own resources             |

## Profiles

| Profile            | Database       | Logging |
|--------------------|----------------|---------|
| `local`            | localhost:3306 | DEBUG   |
| `docker`           | gymappMySqlContainer:3306 | INFO    |
| `dev`              | dev-db:3306    | DEBUG   |
| `stg`              | stg-db:3306    | INFO    |
| `prod`             | prod-db:3306   | WARN    |
| `integration-test` | TestContainers | DEBUG   |

```bash
java -jar target/spring-gym-1.1-SNAPSHOT.jar --spring.profiles.active=local
```

## Docker Services

| Service    | Port | Purpose    |
|------------|------|------------|
| MySQL      | 3306 | Database   |
| Prometheus | 9090 | Metrics    |
| Grafana    | 3000 | Dashboards |

```bash
docker-compose up -d # Start
docker-compose down -v # Stop + delete data
```

## Testing

```bash
./mvnw test                      # Unit tests only (~5s)
./mvnw test -P integration-test  # Integration tests with MySQL
xdg-open target/site/jacoco/index.html # Coverage report
```

- **Unit tests**: Fast, no external dependencies
- **Integration tests**: TestContainers + MySQL (tagged with `@Tag("integration")`)
- **JaCoCo** enforces 80% line coverage (with exclusions)

## Project Structure

```
src/main/java/.../gym/
├── controller/ # REST endpoints + GlobalExceptionHandler
├── service/ # Business logic
├── repository/ # Spring Data JPA
├── model/ # JPA entities
├── dto/ # Request/Response objects
├── security/ # JWT, Spring Security, @SelfService AOP
├── metrics/ # Custom Prometheus metrics
└── actuator/health/ # Custom health indicators
```

## Observability

- **Transaction ID**: `X-Transaction-ID` header (auto-generated, logged via MDC)
- **Custom Metrics**: `gym.users`, `gym.training.created.total`, etc.
- **Health Indicators**: Database, Flyway, TrainingType validation
- **Logs**: Rolling files in `logs/` (14 days retention)

## License

Educational / learning project.
