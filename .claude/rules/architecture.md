## Architecture

Hexagonal Architecture (Ports & Adapters) multi-module Gradle project.

**Dependency direction (one-way):**
```
module-adaptor → module-core:port → module-core:application → module-core:domain
```

`module-core:domain` has **zero** framework dependencies — pure Java.

**Module roles:**
- `module-bootstrap` — Spring Boot entry point, application config
- `module-core/domain` — Domain models and business rules (pure Java)
- `module-core/application` — Application services, orchestration, mappers
- `module-core/port` — Repository/service interface contracts and DTOs
- `module-adaptor/inbound/api` — REST controllers (`*Api`) and use case orchestrators (`*UseCase`)
- `module-adaptor/outbound/rds` — JPA entities and `*RepositoryImpl`
- `module-adaptor/outbound/cache` — Redis configuration
- `module-adaptor/outbound/external` — OpenFeign clients

**Error handling pattern — sealed types:**
- `CommandResult<T>`: `Success` / `ValidationError` / `BusinessError` (domain layer)
- `RepositoryResult<T>`: `Found` / `NotFound` / `Error` (port layer)
- `ApiError`: `NotFound` / `BadRequest` / `InternalError` (API layer)