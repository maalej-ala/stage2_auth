# Stage 1: Build with Maven
FROM eclipse-temurin:21-jdk-jammy as builder

WORKDIR /workspace/app

# Copy Maven wrapper (if using mvnw)
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# Download dependencies (cached unless POM changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application (creates a FAT JAR)
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built JAR (adjust if your JAR has a different naming pattern)
COPY --from=builder /workspace/app/target/*.jar app.jar

EXPOSE 8080

# Optimized startup with JVM parameters
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "-Dserver.port=8080", "app.jar"]