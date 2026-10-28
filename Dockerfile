# Multi-stage Docker build for NirmaanSetu Spring Boot backend
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace

# Copy maven wrapper / pom and cache dependencies
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd ./

# Download dependencies offline
RUN chmod +x ./mvnw || true
RUN ./mvnw dependency:go-offline -B || true

# Copy source and database migration files
COPY src src
COPY db db

# Package application
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal, secure JRE runtime
FROM eclipse-temurin:17-jre-jammy
LABEL maintainer="NirmaanSetu Engineering <dev@nirmaansetu.in>"

WORKDIR /app

# Create unprivileged application user
RUN groupadd -r nirmaan && useradd -r -g nirmaan -u 1001 nirmaan

# Create persistent uploads directory
RUN mkdir -p /var/data/uploads && chown -R nirmaan:nirmaan /var/data/uploads

# Copy built jar from builder
COPY --from=builder --chown=nirmaan:nirmaan /workspace/target/*.jar /app/app.jar

USER nirmaan

ENV SERVER_PORT=8080 \
    NIRMAANSETU_UPLOAD_DIR=/var/data/uploads \
    JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
