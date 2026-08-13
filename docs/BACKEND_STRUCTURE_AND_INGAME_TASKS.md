# 백엔드 구조 & 인게임 작업 가이드

백엔드 2명 협업용: **프로젝트 구조**와 **인게임 담당자가 할 작업**을 한눈에 보는 문서입니다.

---

## 1. 프로젝트 구조 (한눈에)

```
cnr/
├── module-bootstrap/              ← 앱 진입점 (main, 설정). 보통 수정 적음
├── module-utils/                  ← 공통 유틸
│
├── module-core/                   ← ★ 비즈니스 로직 (둘 다 여기서 작업)
│   ├── domain/                    ← 도메인 모델·이벤트 (순수 Java)
│   │   ├── common/               ← CommandResult 등
│   │   ├── foo/                   ← 예시 CRUD (참고용)
│   │   ├── user/                  ← User, OAuth
│   │   ├── room/                  ← Room, RoomPlayer, MapZone, RoomSettings ...
│   │   └── game/                  ← GameState, InGamePlayer, Gem, GameEvent, Location 관련 ...
│   │
│   ├── application/               ← 서비스 (유즈케이스 오케스트레이션 아래의 실제 로직)
│   │   ├── room/                  ← RoomService
│   │   ├── game/                  ← GameStateService, GameActionService, LocationService,
│   │   │                            GemService, GameEventService, GameTimerService ...
│   │   └── user/                  ← UserService, OAuthUserService
│   │
│   └── port/                      ← 인터페이스 (저장소·외부 연동 계약)
│       ├── common/                ← RepositoryResult
│       ├── room/                  ← RoomStore, DTO
│       ├── game/                  ← GameStateStore, InGamePlayerStore, LocationStore,
│       │                            LocationPublisher/Subscriber, GemStore, GameEventPublisher ...
│       └── user/, oauth/          ← User, OAuth
│
└── module-adaptor/                ← 외부와 연결 (API·DB·Redis·외부 API)
    ├── inbound/
    │   ├── api/                   ← ★ REST API (Controller + UseCase)
    │   │   ├── common/            ← 에러, Swagger, Security 어댑터
    │   │   ├── auth/              ← 인증 (다른 분 담당 가능)
    │   │   ├── room/              ← 방 API (다른 분 담당 가능)
    │   │   └── game/              ← ★ 인게임 API 전부 (Location 담당자 주로 여기)
    │   │       ├── GameStateApi.java
    │   │       ├── GameActionApi.java
    │   │       ├── GameLocationApi.java
    │   │       ├── GameEventApi.java
    │   │       ├── GameGemApi.java
    │   │       ├── request/, response/, usecase/
    │   │       └── ...
    │   ├── security/              ← JWT, OAuth 필터 (인증 담당)
    │   └── batch/                ← 타이머, 보석 스폰 스케줄러
    │
    └── outbound/
        ├── rds/                   ← PostgreSQL (User, Foo 등)
        └── cache/                ← ★ Redis (방·게임·위치·이벤트 전부)
            └── game/             ← LocationRedisStore, GameStateRedisStore, ...
```

**인게임(위치/게임로직) 담당자가 자주 만지는 곳**

- **API 추가/수정**: `module-adaptor/inbound/api/.../api/game/` + `request/`, `response/`, `usecase/`
- **비즈니스 로직**: `module-core/application/.../game/`
- **도메인 모델/이벤트**: `module-core/domain/.../game/`
- **저장/조회 계약**: `module-core/port/.../game/`
- **Redis 구현**: `module-adaptor/outbound/cache/.../game/`

**요청 흐름 (인게임 예시)**

```
클라이언트
  → GameXxxApi (Controller)  [module-adaptor/inbound/api]
  → GameXxxUseCase           [같은 패키지]
  → XxxService               [module-core/application/game]
  → Port (LocationStore 등)  [module-core/port]
  → XxxRedisStore 등         [module-adaptor/outbound/cache]
  → Redis
```

---

## 2. 작업 목록 vs 현재 구현 상태

| 구분 | 작업 | 현재 상태 | 비고 |
|------|------|-----------|------|
| **인증** | 로그인 API | ✅ 구현됨 | `/v1/auth/token`, `/oauth`, `/refresh` |
| **방** | 방 생성 API | ✅ | `POST /v1/rooms` |
| | 방 참여 API | ✅ | `POST /v1/rooms/{roomId}/join` |
| | 방 정보 조회 API | ✅ | `GET /v1/rooms/{roomId}` |
| | 게임 시작 API | ✅ | `POST /v1/rooms/{roomId}/start` |
| | 게임 상태 조회 API | ✅ | `GET /v1/game/{gameId}` (상태·시작/종료 시각) |
| **게임 로직** | 게임 종료 판별 | ✅ 내부 로직 | 타이머 + 전원 체포 시 `endGame()` 호출 |
| | 남은 시간 조회 API | ⚠️ 보강 가능 | `GET /v1/game/{gameId}` 에 `endsAt` 있음 → 클라이언트에서 `endsAt - now` 또는 전용 필드 추가 |
| **체포** | 도둑 체포 요청 API | ✅ | `POST /v1/game/{gameId}/arrest` |
| | 체포 결과 조회 API | ⚠️ 보강 가능 | `GET /v1/game/{gameId}/players/{playerId}` 에 상태 포함. 전용 API 필요 시 추가 |
| **위치** | 사용자 위치 업데이트 API | ✅ | `POST /v1/game/location` (gameId, playerId, lat, lon) |
| | 내 현재 위치 조회 API | ❌ 없음 | `LocationStore.getLocation` 있음 → API만 추가 |
| | 특정 유저 위치 조회 API | ❌ 없음 | 위와 동일 포트 사용 → API 추가 |
| **Geo** | 경찰 반경 내 도둑 조회 API | ❌ 없음 | 거리 로직은 체포 시 사용 중 → 조회 API 신규 |
| | 거리 계산 (내부 로직) | ✅ | `LocationStore.getDistanceMeters`, haversine in GameActionService |
| | 게임 구역 포함 여부 (내부) | ✅ | `PolygonUtils.contains` (감옥 이탈 등) |
| | 위험 구역 진입 여부 (내부) | ⚠️ 설계만 | MapZone에 제한 구역 있으면 확장 가능 |
| **상태** | 유저 상태 변경 API | ⚠️ 내부 전용 | 체포/구출 시 상태 변경. 별도 “상태 변경 API” 필요 여부 협의 |
| | 방 상태 변경 API | ⚠️ 내부 전용 | 게임 시작 등으로 변경. 별도 API 필요 여부 협의 |

---

## 3. 인게임 API/기능 구현 순서 (추천)

의존성과 난이도를 고려한 순서입니다. **한 번에 하나씩** 진행하는 걸 권장합니다.

### 1단계: 위치 조회 API (기반 잡기)

- **목표**: 이미 있는 “위치 업데이트”와 저장소를 활용해, 조회 API만 추가.
- **작업**
  - [ ] **내 현재 위치 조회 API**  
    - 예: `GET /v1/game/{gameId}/location/me`  
    - `GameLocationUseCase` + `LocationService`에서 `LocationStore.getLocation(gameId, playerId)` 호출 후 응답 DTO 반환.
  - [ ] **특정 유저 위치 조회 API**  
    - 예: `GET /v1/game/{gameId}/location/{playerId}`  
    - (정책: 같은 팀만? 전체? 등은 팀과 협의 후 적용.)
- **수정 위치**: `api/game/GameLocationApi.java`, `game/usecase/GameLocationUseCase`, `game/response/LocationResponse.java` (이미 있으면 재사용).

→ 이렇게 하면 “위치 기반” 나머지 기능(거리, 반경 내 도둑 등)이 모두 “위치 조회” 위에 올라갑니다.

### 2단계: Geo – 거리·반경 (체포/시야와 연결)

- **목표**: 거리 계산은 이미 내부에 있으니, “경찰 반경 내 도둑”을 서비스·API로 노출.
- **작업**
  - [ ] **경찰 반경 내 도둑 조회 API**  
    - 예: `GET /v1/game/{gameId}/geo/robbers-nearby?playerId=경찰Id&radiusMeters=50`  
    - Application: `LocationStore.getLocation` + `getDistanceMeters`(또는 Redis GEORADIUS 활용)로 반경 내 도둑 목록 반환.  
    - 기존 체포 로직과 반경 정책(미터 수) 맞추기.
  - [ ] **거리 계산 처리 API** (필요 시)  
    - 내부 로직만 쓸 거면 생략 가능.  
    - 필요 시 예: `GET /v1/game/{gameId}/geo/distance?from=playerId1&to=playerId2` → `LocationStore.getDistanceMeters` 노출.
- **수정 위치**:  
  - `module-core/application/game/` 에 `GeoService` 또는 기존 `LocationService` 확장.  
  - `module-adaptor/inbound/api/game/` 에 `GameGeoApi` (또는 GameLocationApi에 경로 추가), request/response, usecase.

### 3단계: 게임 구역·위험 구역 (내부 → API 노출 여부)

- **목표**: “게임 구역 포함 여부”, “위험 구역 진입 여부”는 이미 `PolygonUtils.contains` 등으로 내부 사용 중. 클라이언트가 직접 물어볼 필요가 있으면 API로.
- **작업**
  - [ ] **게임 구역 포함 여부 체크 API** (필요 시)  
    - 예: `POST /v1/game/{gameId}/geo/in-play-area` Body: `{ "latitude", "longitude" }`  
    - Response: `{ "inside": true/false }`  
    - Application에서 `GameStateStore`로 설정 조회 → `playArea` 폴리곤으로 `PolygonUtils.contains` 호출.
  - [ ] **위험 구역(제한 구역) 진입 여부 체크 API** (설계에 있으면)  
    - MapZone에 제한 구역(폴리곤)이 있으면, 동일하게 `contains` 또는 “진입 여부” 로직 추가 후 API로 노출.
- **수정 위치**:  
  - Application: `PolygonUtils` 사용하는 서비스 메서드 추가.  
  - API: `GameGeoApi` 또는 별도 `GeoUseCase`에서 호출.

### 4단계: 게임·체포 결과 보강 (선택)

- **목표**: “남은 시간”, “체포 결과”를 더 명시적으로.
- **작업**
  - [ ] **남은 시간 조회**  
    - `GET /v1/game/{gameId}` 응답에 `remainingSeconds` (또는 `endsAt` 유지) 추가.  
    - Application/UseCase에서 `endsAt - System.currentTimeMillis()` 계산해 넣기.
  - [ ] **체포 결과 조회 API** (전용이 필요하면)  
    - 예: `GET /v1/game/{gameId}/arrest/result` 또는 기존 `GET /v1/game/{gameId}/players/{playerId}` 로 충분한지 팀과 결정 후, 필요 시 전용 엔드포인트 추가.

### 5단계: 유저/방 상태 변경 API (필요 시)

- **목표**: “유저 상태 변경”, “방 상태 변경”이 **관리/디버깅용**으로 필요하면 명시적 API를 추가.  
  인게임에서는 보통 체포/구출/게임시작 등으로 **내부에서만** 상태가 바뀌므로, 팀과 “진짜 필요한지” 먼저 협의하는 걸 권장.
- **작업**
  - [ ] 필요 시에만:  
    - 유저 상태 변경: `PATCH /v1/game/{gameId}/players/{playerId}/status` (ALIVE/ARRESTED 등).  
    - 방 상태 변경: 보통 서버 내부 전용이므로, 운영용 API가 필요할 때만 추가.

---

## 4. 인게임 작업 체크리스트 (요약)

| 순서 | 작업 | API/위치 | 우선순위 |
|------|------|----------|----------|
| 1 | 내 현재 위치 조회 | `GET /v1/game/{gameId}/location/me` | 높음 |
| 2 | 특정 유저 위치 조회 | `GET /v1/game/{gameId}/location/{playerId}` | 높음 |
| 3 | 경찰 반경 내 도둑 조회 | `GET /v1/game/{gameId}/geo/robbers-nearby?...` | 높음 |
| 4 | 거리 계산 API (필요 시) | `GET /v1/game/{gameId}/geo/distance?...` | 중간 |
| 5 | 게임 구역 포함 여부 (필요 시) | `POST /v1/game/{gameId}/geo/in-play-area` | 중간 |
| 6 | 위험 구역 진입 여부 (설계 반영 시) | 동일 Geo 패키지에 추가 | 중간 |
| 7 | 남은 시간 보강 | `GameStateResponse`에 `remainingSeconds` 등 | 낮음 |
| 8 | 체포 결과 조회 전용 (필요 시) | 팀 협의 후 | 낮음 |
| 9 | 유저/방 상태 변경 API | 필요 시만 | 낮음 |

---

## 5. 협업 시 참고

- **인게임(위치/Geo/게임로직)**  
  - 주로: `module-core/domain/game`, `module-core/application/game`, `module-core/port/game`, `module-adaptor/inbound/api/.../game`, `module-adaptor/outbound/cache/.../game`.
- **인증/방**  
  - `api/auth`, `api/room`, `application/room`, `application/user`, `inbound/security`, `outbound/rds/user` 등은 상대방이 담당할 수 있음.
- **새 API 추가 시**  
  - Controller → UseCase → Application Service → Port 순서로 추가하면, 기존 패턴과 동일하게 유지됩니다.
  - 응답/에러는 `CommandResult` + `ApiError` / `ResponseMapper` 사용 (기존 game API 참고).

이 순서대로 하면 “위치 업데이트”는 이미 있으니, **위치 조회 → Geo(거리/반경/구역) → 나머지 보강** 순으로 한 개씩 진행할 수 있습니다.
