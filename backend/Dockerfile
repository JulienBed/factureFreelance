# Multi-stage build for Quarkus
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .

# Download dependencies - use dependency:resolve which properly handles BOMs
# The || true ensures the build continues even if some optional deps can't be resolved
RUN mvn dependency:resolve-plugins dependency:resolve -B || true

# Copy source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built artifact from build stage
COPY --from=build /app/target/quarkus-app/ ./

EXPOSE 8080

CMD ["java", "-jar", "quarkus-run.jar"]
