# spring-gym

A **microservices-based gym management (CRM) system** built with Spring Boot,
featuring JWT authentication, asynchronous messaging,
OpenAPI documentation, and Prometheus/Grafana monitoring.

## Architecture

```
┌──────────────┐       ┌──────────────────┐       ┌───────────────────┐
│              │  JMS  │                  │       │                   │
│   Main App   ├──────►│ ActiveMQ Artemis ├──────►│  Workload Service │
│  (port 8080) │       │   (port 61616)   │       │   (port 8082)     │
│              │       │                  │       │                   │
└──────────────┘       └──────────────────┘       └───────────────────┘
```

| Service              | Description                                   | Database       |
|----------------------|-----------------------------------------------|----------------|
| **Main App**         | Gym CRM — trainees, trainers, trainings, auth | MySQL 9.x      |
| **Workload Service** | Trainer workload summaries (hours per month)  | MongoDB        |
| **ActiveMQ Artemis** | Async messaging between main app and workload | —              |

## Tech Stack

- **Spring Boot 4.0.x** (Web MVC, Data JPA, Validation, Actuator)
- **Spring Cloud 2025.1.1** (Eureka Discovery)
- **ActiveMQ Artemis** (JMS async messaging, dead letter queue)
- **Spring Security** + **BCrypt** password hashing
- **MySQL 9.x** + **Flyway** migrations (main app)
- **MongoDB** (workload service — document store with embedded year/month summaries)
- **JWT** authentication (jjwt 0.13.0, shared secret across services)
- **OpenAPI 3.0** + Swagger UI
- **Micrometer** + Prometheus + Grafana
- **Testcontainers** + JaCoCo (80% coverage enforced, with exclusions)
- **Java 21**

## Quick Start

### Infrastructure Only (local development)

Start MySQL, MongoDB, ActiveMQ Artemis:

```bash
cd docker
docker compose up -d
```

Then run the apps from your IDE or terminal:

```bash
# Terminal 1 — Workload Service
cd spring-gym-workload
./mvnw spring-boot:run

# Terminal 2 — Main App
# cd ~/code/java/spring-gym
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Full Stack (everything in Docker)

```bash
cd docker
docker compose --profile apps --profile monitoring up -d
```

### Selective Profiles

```bash
# Infrastructure + apps (no monitoring)
docker compose --profile apps up -d

# Infrastructure + monitoring (run apps locally)
docker compose --profile monitoring up -d

# Stop everything
docker compose --profile apps --profile monitoring down
```

### Access

| Resource          | URL                                     |
|-------------------|-----------------------------------------|
| Swagger UI        | http://localhost:8080/swagger-ui.html   |
| API Base          | http://localhost:8080/api/v1            |
| Health (Main)     | http://localhost:8081/actuator/health   |
| Health (Workload) | http://localhost:8082/actuator/health   |
| Artemis Console   | http://localhost:8161 (artemis/artemis) |
| Grafana           | http://localhost:3000 (admin/admin)     |

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

### Workload Service

| Method | Endpoint                      | Description                                 |
|--------|-------------------------------|---------------------------------------------|
| GET    | `/api/v1/workload/{username}` | Get trainer workload summary (JWT required) |

Workload data is populated asynchronously via JMS messages from the main app.

### Auth Flow

```
1. POST /api/v1/trainees → { username, password }
2. POST /api/v1/auth/login → { token, role }
3. Use header: Authorization: Bearer <token>
```

## Messaging

The main app sends workload notifications to the workload service via ActiveMQ Artemis:

| Event            | Action                | Queue            |
|------------------|-----------------------|------------------|
| Training created | ADD                   | `workload.queue` |
| Training deleted | DELETE                | `workload.queue` |
| Trainee deleted  | DELETE (per training) | `workload.queue` |

- Messages are sent **after the DB transaction commits**
- **Dead letter queue**: Invalid messages are redelivered by Artemis and moved to `DLQ` after max delivery attempts
- **Transaction ID** is propagated as a JMS message property for end-to-end tracing

## Security Features

| Feature                        | Description                                           |
|--------------------------------|-------------------------------------------------------|
| **BCrypt Hashing**             | Passwords hashed with BCrypt encoder                  |
| **JWT Tokens**                 | 24-hour expiration, blacklisted on logout             |
| **Brute Force Protection**     | 3 failed attempts → 5 min lockout (429 + Retry-After) |
| **Token Blacklist**            | Logged-out tokens rejected until expiration           |
| **Self-Service Authorization** | Users can only access their own resources             |
| **Shared JWT Secret**          | Both services validate tokens with the same secret    |

## Profiles

| Profile            | Database          | Logging |
|--------------------|-------------------|---------|
| `local`            | localhost:3306    | DEBUG   |
| `docker`           | gymapp-mysql:3306 | INFO    |
| `dev`              | dev-db:3306       | DEBUG   |
| `stg`              | stg-db:3306       | INFO    |
| `prod`             | prod-db:3306      | WARN    |
| `integration-test` | TestContainers    | DEBUG   |

```bash
java -jar target/spring-gym-1.1-SNAPSHOT.jar --spring.profiles.active=local
```

## Docker Services

| Service          | Port(s)     | Image                           |
|------------------|-------------|---------------------------------|
| MySQL            | 3306        | mysql:9.5                       |
| ActiveMQ Artemis | 61616, 8161 | apache/activemq-artemis:2.40.0  |
| Main App         | 8080, 8081  | iasw/spring-gym:latest          |
| Workload Service | 8082        | iasw/spring-gym-workload:latest |
| Prometheus       | 9090        | prom/prometheus:v2.53.0         |
| Grafana          | 3000        | grafana/grafana:11.3.0          |

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
spring-gym/                          # Main app
├── src/main/java/.../gym/
│   ├── controller/                  # REST endpoints + GlobalExceptionHandler
│   ├── service/                     # Business logic
│   ├── repository/                  # Spring Data JPA
│   ├── model/                       # JPA entities
│   ├── dto/                         # Request/Response objects
│   ├── client/                      # Workload notification (JMS producer)
│   ├── config/                      # Security, JMS, Swagger config
│   ├── security/                    # JWT, Spring Security, @SelfService AOP
│   ├── metrics/                     # Custom Prometheus metrics
│   └── actuator/health/             # Custom health indicators
└── spring-gym-workload/             # Workload microservice
    └── src/main/java/.../workload/
        ├── controller/              # GET workload endpoint
        ├── listener/                # JMS message consumer
        ├── service/                 # Workload processing logic
        ├── model/                   # TrainerWorkload + MonthlySummary
        ├── dto/                     # WorkloadRequest/Response
        ├── repository/              # Spring Data JPA (H2)
        ├── config/                  # Security, JMS config
        └── security/                # JWT validation, TransactionId filter
```

## Observability

- **Transaction ID**: `X-Transaction-ID` header (auto-generated, logged via MDC)
- **Custom Metrics**: `gym.users`, `gym.training.created.total`, etc.
- **Health Indicators**: Database, Flyway, TrainingType validation
- **Logs**: Rolling files in `logs/` (14 days retention)

## License

Educational / learning project.
