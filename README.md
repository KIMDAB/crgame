# CNR (Cops N Robbers)

Java 21 + Spring Boot 3.5 기반의 **헥사고날 아키텍처(Hexagonal Architecture)** 멀티모듈 프로젝트입니다.

## 프로젝트 요약

- **언어**: Java 21 (record, sealed class 등 최신 문법 활용)
- **프레임워크**: Spring Boot 3.5.x
- **빌드**: Gradle Groovy DSL, 멀티모듈 구조
- **아키텍처**: 헥사고날 아키텍처 (Ports & Adapters)

비즈니스 로직(도메인)을 외부 인프라(DB, API, 이벤트 등)로부터 완전히 격리하여, 인프라 변경 시 핵심 로직에 영향을 주지 않는 구조를 목표로 합니다.

## 모듈 구조

```
copsNrobbers/
├── module-bootstrap/           ← 애플리케이션 진입점 (Spring Boot main)
├── module-utils/               ← 공통 유틸리티 (스켈레톤)
├── module-core/                ← 핵심 비즈니스 로직
│   ├── domain/                 ← 도메인 모델 (순수 Java, 외부 의존성 없음)
│   ├── application/            ← 애플리케이션 서비스 (재사용 가능한 서비스)
│   └── port/                   ← 포트 인터페이스 (외부와의 계약 정의)
└── module-adaptor/             ← 외부 시스템 연결
    ├── inbound/                ← 외부 → 내부 (요청 수신)
    │   ├── api/                ← REST API 컨트롤러 (+유즈케이스 오케스트레이션)
    │   ├── event/              ← 이벤트 리스너 (스켈레톤)
    │   └── batch/              ← 배치 작업 (스켈레톤)
    └── outbound/               ← 내부 → 외부 (데이터 저장/호출)
        ├── rds/                ← RDS 저장소 구현
        └── external/           ← 외부 API 클라이언트
            └── foo-client/     ← Foo 외부 서비스 연동 (스켈레톤)
```

## 모듈 간 의존성 관계

```
module-bootstrap
├── module-adaptor:inbound:api
├── module-core:application
└── module-adaptor:outbound:rds

module-adaptor:inbound:api
├── module-core:application
└── module-core:domain

module-core:application
├── module-core:domain
└── module-core:port

module-adaptor:outbound:rds
└── module-core:port

module-adaptor:outbound:external:foo-client
└── module-core:port
```

> **핵심 원칙**: 의존성은 항상 **바깥 → 안쪽**으로 흐릅니다. `domain` 모듈은 어떤 모듈에도 의존하지 않으며, `port` 모듈도 외부 프레임워크 의존성이 없습니다.

## 데이터 흐름

HTTP 요청이 처리되는 전체 흐름을 예시(`Foo` 도메인)로 설명합니다:

```
[클라이언트] ──HTTP──▶ FooApi (REST Controller)
                          │
                          ▼
                      FooUseCase (유즈케이스 오케스트레이션)
                          │
                          ▼
                    FooQueryService (애플리케이션 서비스)
                          │
                    ┌─────┴──────┐
                    ▼            ▼
              Foo (도메인)   FooRepository (포트 인터페이스)
              비즈니스 로직         │
                                 ▼
                          FooRepositoryImpl (어댑터 구현)
                                 │
                                 ▼
                            [데이터 저장소]
```

### 요청 흐름 상세 (Foo 조회 예시)

1. `FooApi`가 HTTP GET `/v1/foo/{id}` 요청을 수신
2. `FooUseCase.findFooWithBusinessLogic(id)`을 호출 → `CommandResult<FooResponse>` 반환
3. `FooQueryService.findById(id)`가 `FooRepository` 포트를 통해 데이터 조회
4. `FooRepositoryImpl`이 실제 저장소에서 `FooEntity`를 조회 → `RepositoryResult<FooDto>` 반환
5. `FooMapper.toDomain(dto)`으로 도메인 객체 `Foo`로 변환
6. `Foo.fooBusinessLogic()`으로 비즈니스 로직 수행
7. UseCase가 `FooResponse.from(foo)`로 응답 표현 객체 생성 → `CommandResult<FooResponse>` 반환
8. Controller가 `CommandResult`를 switch 패턴 매칭으로 `ResponseEntity`로 변환 후 반환

### Sealed 타입 기반 데이터 흐름

예외를 던지는 대신 sealed 타입으로 성공/실패를 표현하여, 컴파일 타임에 모든 케이스를 처리하도록 강제합니다.

```
[Repository]  ──RepositoryResult──▶  [Service]  ──CommandResult──▶  [UseCase]  ──switch 패턴매칭──▶  [Controller]  ──ResponseEntity──▶  [Client]
```

- `RepositoryResult<T>`: Found / NotFound / Error (module-core:port)
- `CommandResult<T>`: Success / ValidationError / BusinessError (module-core:domain)
- `ApiError`: NotFound / BadRequest / InternalError (module-adaptor:inbound:api)

### 계층별 데이터 변환 흐름

```
FooCreateRequest → FooCreateCommand → FooCreateDto → FooEntity → [저장소]
FooUpdateRequest → FooUpdateCommand → FooUpdateDto → FooEntity → [저장소]
[저장소] → FooEntity → RepositoryResult<FooDto> → CommandResult<Foo> → FooResponse / ApiErrorResponse
```

## 문제별 참고 모듈 가이드

| 상황 | 참고할 모듈 README                                                                  |
|---|--------------------------------------------------------------------------------|
| 새로운 도메인/엔티티를 추가하고 싶다 | [module-core/domain](module-core/domain/README.md)                             |
| 비즈니스 로직을 수정하고 싶다 | [module-core/domain](module-core/domain/README.md)                             |
| 서비스 레이어를 수정/추가하고 싶다 | [module-core/application](module-core/application/README.md)                   |
| 포트(인터페이스)를 정의하고 싶다 | [module-core/port](module-core/port/README.md)                                 |
| REST API 엔드포인트를 추가/수정하고 싶다 | [module-adaptor/inbound/api](module-adaptor/inbound/api/README.md)             |
| DB 관련 구현을 수정하고 싶다 | [module-adaptor/outbound/rds](module-adaptor/outbound/rds/README.md)           |
| 외부 API 연동을 추가하고 싶다 | [module-adaptor/outbound/external](module-adaptor/outbound/external/README.md) |
| 배치 작업을 추가하고 싶다 | [module-adaptor/inbound/batch](module-adaptor/inbound/batch/README.md)         |
| 이벤트 처리를 추가하고 싶다 | [module-adaptor/inbound/event](module-adaptor/inbound/event/README.md)         |
| 공통 유틸리티를 추가하고 싶다 | [module-utils](module-utils/README.md)                                         |
| 애플리케이션 설정/실행 문제가 있다 | [module-bootstrap](module-bootstrap/README.md)                                 |
| 프로젝트 전체 구조를 이해하고 싶다 | 이 문서 (README.md)                                                               |

## feature/geo — 시야·거리(Geo) 기능

> **이 브랜치**에서는 **시야/거리(Geo)** 기능(경찰 반경 내 도둑 조회 등)을 개발합니다.  
> API 스펙·반경 설정·응답 형식은 **[docs/README-feature-geo.md](docs/README-feature-geo.md)** 에 정리되어 있습니다.

## 빠른 시작

### 사전 요구사항

- Java 21+
- Docker & Docker Compose

### 0. 환경 변수 설정 (.env)

프로젝트 루트에 `.env` 파일을 생성해야 애플리케이션을 실행할 수 있습니다.

```bash
# 프로젝트 루트에 .env 파일 생성
cp .env.example .env
# 이후 .env 파일을 열어 각 값을 채워넣습니다
```

| 변수명 | 설명 |
|---|---|
| `RDS_POSTGRES_USERNAME` | PostgreSQL 접속 사용자명 |
| `RDS_POSTGRES_PASSWORD` | PostgreSQL 접속 비밀번호 |
| `CACHE_VALKEY_PASSWORD` | Valkey(Redis) 접속 비밀번호 |
| `JWT_PRIVATE_KEY` | JWT 서명에 사용할 비밀키 |

> **참고1**: `.env` 파일은 `.gitignore`에 포함되어 있으므로 저장소에 커밋되지 않습니다. 팀원에게 별도로 공유받으세요.
> 
> **참고2**: 인텔리제이 프로젝트 실행 시 `.env` 파일 인식에 실패할 경우, Run/Debug Configurations에서 "Environment variables" 항목에 `.env` 파일 경로를 추가해주면 됩니다.

### 1. 인프라 실행 (PostgreSQL, Redis)

```bash
# Docker로 로컬 DB/Redis 실행
docker compose up -d

# 상태 확인
docker compose ps

# 로그 확인
docker compose logs -f
```

| 서비스 | 포트 | 접속 정보 |
|---|---|---|
| PostgreSQL | `localhost:5432` | DB: `cnr`, User: `cnr`, PW: `cnr1234` |
| Redis | `localhost:6379` | - |

### 2. 애플리케이션 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew :module-bootstrap:bootRun

# 테스트
./gradlew test
```

### 3. API 동작 확인

```bash
# 전체 Foo 조회
curl http://localhost:8080/v1/foo

# ID로 Foo 조회
curl http://localhost:8080/v1/foo/1

# Foo 생성
curl -X POST http://localhost:8080/v1/foo \
  -H "Content-Type: application/json" \
  -d '{"name": "New Foo", "description": "새로운 Foo"}'
```

### 4. 종료

```bash
# 인프라 종료 (데이터 유지)
docker compose down

# 인프라 종료 + 데이터 삭제
docker compose down -v
```

> **운영 환경**: PostgreSQL은 [Supabase](https://supabase.com)로 전환 예정입니다. `application.properties`의 DB 접속 정보만 변경하면 됩니다.

## 기술 스택

| 영역 | 기술 |
|---|---|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 3.5.x |
| 빌드 | Gradle 8.x (Groovy DSL) |
| ORM | Spring Data JPA + QueryDSL 5.0 |
| DB | PostgreSQL 17 (로컬), Supabase (운영 예정) |
| 캐시 | Redis 7 |
| 외부 API | Spring Cloud OpenFeign |
| 배치 | Spring Batch |
| 컨테이너 | Docker Compose |
