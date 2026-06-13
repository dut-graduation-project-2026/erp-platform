# --- Stage 1: Build the application ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY src src
RUN --mount=type=cache,target=/root/.m2 sh mvnw clean package -DskipTests

# --- Stage 2: Runtime image ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /app/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

USER spring:spring

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
