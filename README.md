# spring-gym

A Spring-based gym management application 
built with **Spring Core + Spring ORM (JPA/Hibernate)**, 
supporting **Flyway migrations**, **XML import/export**, 
and **test coverage enforcement**.

This project intentionally avoids Spring Boot starters to demonstrate explicit configuration and layering.

## 🚀 Build

This project uses **Maven** and produces 
a **self-contained executable (uber JAR)** using the **Maven Shade Plugin**.

```bash
./mvnw clean package
```

Output:

```text
target/spring-gym-1.1-SNAPSHOT.jar
```

ℹ️ The shaded JAR includes all runtime dependencies and can be executed directly with java -jar.
```bash
java -jar target/spring-gym-1.1-SNAPSHOT.jar
```

## 🧱 Runtime Requirements
This application requires the following to run locally:

### Java
- Java 21

### Database:

- MySQL (8.x or newer)
- It can be managed via Docker + Docker Compose
- No local MySQL installation is required when using Docker.


## 🐳 Running MySQL with Docker
A sample docker-compose.yml is provided to start a compatible MySQL instance.

### Prerequisites
- Docker
- Docker Compose
### Start MySQL
```bash
docker-compose up -d
```
Stop & clean database data
```bash
docker-compose down -v
```
⚠️ Removing volumes (-v) deletes all database data.



## 🧩 Runtime Profiles

The application supports three profiles:

| Profile    | Description                                 |
|------------|---------------------------------------------| 
| (none)     | Default DB-backed runtime (MySQL + Flyway)  |
| xml-read   | Reads data from an external XML file        | 
| xml-write  | Writes data to an external XML file         | 
Profiles are activated using:

```
-Dspring.profiles.active=<profile>
```

## 🗄 Database & Migrations
- Database: MySQL (tested with MySQL 8.x and 9.x)
- ORM: Hibernate (JPA)
- Connection Pool: HikariCP
- Migrations: Flyway
- Flyway migrations are executed automatically on startup.

✅ Flyway works correctly in the shaded JAR 
thanks to explicit ServiceLoader resource merging in the Shade plugin.



## 📁 External XML Storage

When using xml-read or xml-write, the following property is used:

```text
gymapp.file.storage.path=file:./gym-external-data.xml
```

This path points to an **external XML** file used for import/export.

## ▶️ Running the Application

### Default runtime (DB + Flyway)

```bash
java -jar target/spring-gym-1.1-SNAPSHOT.jar
```

### XML Read Profile

```bash
java \
-Dspring.profiles.active=xml-read \
-Dgymapp.file.storage.path=file:./gym-external-data.xml \
-jar target/spring-gym-1.1-SNAPSHOT.jar
```

### XML Write Profile
```bash
java \
-Dspring.profiles.active=xml-write \
-Dgymapp.file.storage.path=file:./gym-external-data.xml \
-jar target/spring-gym-1.1-SNAPSHOT.jar
```

## ⚙️ Configuration Resolution Order

Spring resolves configuration in the following order (highest → lowest priority):

1. JVM system properties (-Dkey=value)
2. Command-line arguments (--key=value)
3. Environment variables
4. application-{profile}.properties
5. application.properties

This means runtime overrides always take precedence.

## 🧪 Testing & Coverage

### Testing Stack

- JUnit 5
- Mockito
- AssertJ
- JaCoCo (coverage enforced)
- Spring-test
- H2 (in-memory DB for integration tests)

### Test Types
- Unit tests (services, utilities)
- Integration tests (repositories with real JPA + H2)

## ✅ Coverage Enforcement

- JaCoCo enforces minimum 80% line coverage
- Coverage is checked during mvn test

### Excluded from coverage checks:
- xmlfileIO/**
- model/**
- dto/**
- config/**
- SpringGymApplication

## Run tests + coverage:

```bash
./mvnw clean test
```

### JaCoCo HTML report:

```text
target/site/jacoco/index.html
```

## 🧠 Notes on Shaded JAR & Flyway
The project uses the Maven Shade Plugin to create a single executable JAR.

To ensure Flyway database plugins are discoverable at runtime, 
the build explicitly merges Java ServiceLoader metadata:

```xml
<transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
```

Without this, Flyway cannot detect database support when running the shaded JAR.

## 📌 Versioning

1.1-SNAPSHOT

```text
SNAPSHOT indicates an in-development, non-final build.
```

## 📄 License
```text
Educational / learning project.
```
