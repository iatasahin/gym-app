# spring-gym

A simple Spring-based gym management application with optional XML persistence.

## 🚀 Build

This project uses **Maven** and produces an **uber JAR** via the Maven Shade Plugin.

```bash
./mvnw clean package
```

Output:

```text
target/spring-gym-1.1-SNAPSHOT.jar
```

## 🧩 Runtime Profiles

The application supports three profiles:

| Profile | Description                          |
| --- |--------------------------------------| 
| (none) | Default in-memory behavior           |
| xml-read | Reads data from an external XML file | 
| xml-write | Writes data to an external XML file  | 

## 📁 External XML Storage

When using xml-read or xml-write, the following property defaults to:

```text
gymapp.file.storage.path=file:./gym-external-data.xml
```

This path points to an external XML file used for persistence.

## ▶️ Running the Application

```bash
java [JVM_OPTIONS] -jar spring-gym-1.1-SNAPSHOT.jar
```

```bash
java -Dspring.profiles.active=<profile> \
     -jar spring-gym-1.1-SNAPSHOT.jar
```

```bash
java -Dspring.profiles.active=<profile> \
     -Dgymapp.file.storage.path=<path> \
     -jar spring-gym-1.1-SNAPSHOT.jar
```

### Default profile (no XML)

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

This means runtime overrides always win.

## 🧪 Testing & Coverage
- JUnit 5
- Mockito
- AssertJ
- JaCoCo (coverage enforced)

### Coverage Rule

Minimum line coverage: 80%

### Excluded from coverage checks:
- xmlfileIO/**
- model/**
- config/**
- SpringGymApplication

### Run tests + coverage:

```bash
./mvnw test
```

### JaCoCo HTML report:

```text
target/site/jacoco/index.html
```

## 🛠 Java Version

```text
Java 21
```

## 📌 Versioning

1.1-SNAPSHOT

```text
SNAPSHOT indicates an in-development, non-final build.
```

## 📄 License
```text
Educational / learning project.
```
