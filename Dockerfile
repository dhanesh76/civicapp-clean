# ---- Stage 1: Build JAR ----
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Copy source code
COPY src ./src

# Build JAR
RUN mvn -q clean package -DskipTests

# ---- Stage 2: Run Spring Boot ----
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the built jar from Stage 1
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
