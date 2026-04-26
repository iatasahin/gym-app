FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
