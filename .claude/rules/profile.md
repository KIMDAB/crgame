## Commands

```bash
# Build
./gradlew build
./gradlew :module-bootstrap:bootJar

# Run
./gradlew :module-bootstrap:bootRun

# Test
./gradlew test
./gradlew :module-core:test   # single module

# Docker (local infra)
docker compose -f docker/docker-compose.yml up -d
docker compose -f docker/docker-compose.yml down -v
```

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## Spring Profiles

- `local` — PostgreSQL on localhost:5432, `ddl-auto=update`, debug logging
- `prod` — Supabase PostgreSQL, Aiven Valkey, `ddl-auto=validate`
