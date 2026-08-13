# module-adaptor:inbound:api

**REST API 인바운드 어댑터** 모듈입니다. HTTP 요청을 받아 내부 비즈니스 로직을 호출하는 컨트롤러와 유즈케이스를 제공합니다.

## 역할

- REST API 엔드포인트 정의 (`@RestController`)
- HTTP Request → 도메인 Command 변환
- `CommandResult` → `ResponseEntity` 변환 (Controller 책임)
- 유즈케이스(UseCase) 오케스트레이션 (응답 표현 객체만 생성)

## 핵심 설계 원칙: 책임 분리

| 계층 | 책임 | 반환 타입 |
|---|---|---|
| **UseCase** | 비즈니스 오케스트레이션, 응답 표현 객체 생성 | `CommandResult<FooResponse>`, `List<FooResponse>` |
| **Controller** | HTTP 관심사 (상태 코드, `ResponseEntity`) | `ResponseEntity<?>` |

> UseCase는 HTTP 관심사(`ResponseEntity`, 상태 코드)를 알지 못합니다. 순수하게 비즈니스 결과를 `CommandResult<응답객체>` 형태로 반환하고, Controller가 이를 `ResponseEntity`로 변환합니다.

## 구조

```
api/
├── build.gradle
└── src/main/java/com/toy/cnr/api/
    ├── common/
    │   ├── config/
    │   │   └── SwaggerConfig.java        ← Swagger 설정
    │   └── error/
    │       ├── ApiError.java             ← API 에러 sealed 타입
    │       ├── ApiErrorResponse.java     ← 에러 응답 JSON 구조
    │       └── GlobalExceptionHandler.java ← 전역 예외 처리기
    └── foo/
        ├── FooApi.java                   ← REST Controller (ResponseEntity 생성)
        ├── request/
        │   ├── FooCreateRequest.java     ← 생성 요청 DTO
        │   └── FooUpdateRequest.java     ← 수정 요청 DTO
        ├── response/
        │   └── FooResponse.java          ← 응답 DTO
        └── usecase/
            └── FooUseCase.java           ← 유즈케이스 (응답 표현 객체만 반환)
```

## 의존성

```groovy
dependencies {
    implementation project(':module-core:application')
    implementation project(':module-core:domain')
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4'
}
```

## 주요 클래스

### FooUseCase.java (유즈케이스)

UseCase는 **응답 표현 객체**(`FooResponse`)만 생성합니다. HTTP 관심사를 포함하지 않습니다.

```java
@Component
public class FooUseCase {
    private final FooQueryService fooQueryService;

    // 응답 표현 객체를 CommandResult에 담아 반환
    public CommandResult<FooResponse> findFooWithBusinessLogic(Long id) {
        return switch (fooQueryService.findById(id)) {
            case CommandResult.Success(var foo, var msg) -> {
                var converted = fooQueryService.doBusinessLogic(foo);
                yield new CommandResult.Success<>(FooResponse.from(converted), msg);
            }
            case CommandResult.ValidationError(var errors) ->
                new CommandResult.ValidationError<>(errors);
            case CommandResult.BusinessError(var reason) ->
                new CommandResult.BusinessError<>(reason);
        };
    }

    public CommandResult<FooResponse> create(FooCreateRequest request) { ... }
    public CommandResult<FooResponse> update(Long id, FooUpdateRequest request) { ... }
    public List<FooResponse> findAll() { ... }
    public void delete(Long id) { ... }
}
```

### FooApi.java (REST Controller)

Controller가 `CommandResult` → `ResponseEntity` 변환을 담당합니다.

```java
@RestController
@RequestMapping("/v1/foo")
public class FooApi {
    private final FooUseCase fooUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return toResponseEntity(fooUseCase.findFooWithBusinessLogic(id), "Foo");
    }

    // CommandResult → ResponseEntity 변환 (switch 패턴 매칭)
    private <T> ResponseEntity<?> toResponseEntity(CommandResult<T> result, String resource) {
        return switch (result) {
            case CommandResult.Success(var data, var msg) ->
                ResponseEntity.ok(data);
            case CommandResult.ValidationError(var errors) ->
                ResponseEntity.badRequest().body(
                    ApiErrorResponse.from(new ApiError.BadRequest("Validation failed", errors))
                );
            case CommandResult.BusinessError(var reason) ->
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiErrorResponse.from(new ApiError.NotFound(resource, reason))
                );
        };
    }
}
```

### ApiError (sealed 에러 타입)

```java
public sealed interface ApiError
    permits ApiError.NotFound, ApiError.BadRequest, ApiError.InternalError {

    record NotFound(String resource, String message) implements ApiError {}
    record BadRequest(String message, List<String> details) implements ApiError {}
    record InternalError(String message) implements ApiError {}
}
```

### GlobalExceptionHandler (전역 예외 처리기)

`@RestControllerAdvice`로 처리되지 않은 예외를 `ApiError` sealed 타입으로 변환하여 일관된 에러 응답을 보장합니다.

### Request/Response 클래스

```java
// Request → Command 변환 메서드 포함
@Schema(description = "Foo 생성 요청")
public record FooCreateRequest(String name, String description) {
    public FooCreateCommand toCommand() { ... }
}

// Domain → Response 변환 정적 팩토리 메서드 포함
@Schema(description = "Foo 응답")
public record FooResponse(Long id, String name, String description) {
    public static FooResponse from(Foo foo) { ... }
}
```

## Swagger

| 항목 | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| API Docs (JSON) | http://localhost:8080/v3/api-docs |

## API 엔드포인트

| HTTP 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/v1/foo` | 모든 Foo 조회 |
| GET | `/v1/foo/{id}` | ID로 Foo 조회 (비즈니스 로직 적용) |
| POST | `/v1/foo` | 새 Foo 생성 |
| PUT | `/v1/foo/{id}` | Foo 수정 |
| DELETE | `/v1/foo/{id}` | Foo 삭제 |

## 도메인 API 예시: Game Event SSE (내부 로직 연동)

게임 진행 중 발생하는 이벤트를 **SSE(Server-Sent Events)** 로 구독합니다.

- **Endpoint**: `POST /v1/game/events/subscribe` (`text/event-stream`)
- **구독 요청**: `GameEventSubscribeRequest` (`gameId`, `playerId`)
- **응답 이벤트**: SSE `event:` 필드에 타입을 넣고, `data:` 로 `GameEventResponse` JSON을 전송합니다.

### 이벤트 타입

| event 타입 | 설명 | data 주요 필드 |
|---|---|---|
| `PLAYER_ARRESTED` | 경찰이 도둑을 체포 | `copsId`, `robberId` |
| `PLAYER_RESCUED` | 도둑이 체포된 동료를 구출 | `rescuerId`, `rescuedId` |
| `PRISON_ESCAPE_WARNING` | 체포된 플레이어가 감옥 범위를 이탈(경고) | `playerId` |
| `RESTRICTED_AREA_ENTERED` | 플레이어가 제한/위험 구역(restrictedArea)에 “진입” | `playerId`, `latitude`, `longitude` |
| `ANNOUNCEMENT` | 방장 공지 | `senderId`, `message` |
| `GAME_STARTED` | 게임 시작 | - |
| `GAME_ENDED` | 게임 종료 | `winnerRole` |
| `ROLE_ASSIGNED` | 인게임 역할 배정 | `playerId`, `role` |
| `GEM_COLLECTED` | 도둑이 보석 획득 | `robberId`, `gemId` |
| `GEM_SPAWNED` | 보석 스폰 | `gemId`, `latitude`, `longitude` |
| `PING_ALERT` | 인게임 핑/알람 | `senderId`, `pingType`, `latitude`, `longitude` |

### “내부 로직”에서 이벤트가 발행되는 조건

이번에 추가한 위치 발행 내부 로직은 `module-core:application`의 `LocationService.publishLocation()`에서 수행되며, 아래 조건에서 GameEvent를 발행합니다.

- **감옥 이탈 경고** (`PRISON_ESCAPE_WARNING`)
  - 플레이어 상태가 `ARRESTED` 이고,
  - 방 설정에 `prisonArea` 폴리곤이 존재하며,
  - 발행된 좌표가 `prisonArea` 폴리곤 **외부**인 경우
- **제한/위험 구역 진입 감지** (`RESTRICTED_AREA_ENTERED`)
  - 방 설정에 `restrictedArea` 폴리곤이 존재하며,
  - 직전 좌표는 폴리곤 **외부**였고,
  - 이번에 발행된 좌표가 폴리곤 **내부**로 “진입”한 경우 (edge-trigger)

## 새로운 API 추가 예시

`Bar` 도메인의 API를 추가하는 경우:

```
api/src/main/java/com/toy/cnr/api/
└── bar/
    ├── BarApi.java
    ├── request/
    │   └── BarCreateRequest.java
    ├── response/
    │   └── BarResponse.java
    └── usecase/
        └── BarUseCase.java
```

### UseCase (HTTP 관심사 없음)

```java
@Component
public class BarUseCase {
    private final BarQueryService barQueryService;

    public CommandResult<BarResponse> findById(Long id) {
        return switch (barQueryService.findById(id)) {
            case CommandResult.Success(var bar, var msg) ->
                new CommandResult.Success<>(BarResponse.from(bar), msg);
            case CommandResult.ValidationError(var errors) ->
                new CommandResult.ValidationError<>(errors);
            case CommandResult.BusinessError(var reason) ->
                new CommandResult.BusinessError<>(reason);
        };
    }
}
```

### Controller (ResponseEntity 생성)

```java
@Tag(name = "Bar", description = "Bar 도메인 API")
@RestController
@RequestMapping("/v1/bar")
public class BarApi {
    private final BarUseCase barUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return toResponseEntity(barUseCase.findById(id), "Bar");
    }

    private <T> ResponseEntity<?> toResponseEntity(CommandResult<T> result, String resource) {
        return switch (result) {
            case CommandResult.Success(var data, var msg) -> ResponseEntity.ok(data);
            case CommandResult.ValidationError(var errors) -> ResponseEntity.badRequest().body(
                ApiErrorResponse.from(new ApiError.BadRequest("Validation failed", errors)));
            case CommandResult.BusinessError(var reason) -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiErrorResponse.from(new ApiError.NotFound(resource, reason)));
        };
    }
}
```
