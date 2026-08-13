# ── Build stage ───────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY . .
RUN ./gradlew :module-bootstrap:bootJar --no-daemon -x test

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/module-bootstrap/build/libs/cnr_application.jar app.jar

EXPOSE 8080

# Profile is controlled by the SPRING_PROFILES_ACTIVE env var (defaults to 'local' from application.yml).
# JAVA_OPTS is honored so memory limits can be tuned per environment.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Duser.timezone=Asia/Seoul -jar app.jar"]
