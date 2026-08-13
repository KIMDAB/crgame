# feature/geo — 시야·거리(Geo) 기능

이 문서는 **feature/geo** 브랜치에서 추가한 **시야/거리 계산(Geo)** 관련 기능을 설명합니다.

---

## 개요

- **브랜치**: `feature/geo`
- **목적**: 인게임에서 위치 기반 조회(경찰 반경 내 도둑 등)를 위한 API 제공
- **기반**: Redis GeoHash(GEODIST)를 이용한 거리 계산

---

## 추가된 기능

### 1. 경찰 반경 내 도둑 조회 API

**경찰**이 자신의 현재 위치를 기준으로, **지정한 반경(미터) 안에 있는 도둑** 목록을 조회합니다.  
각 도둑의 **위도·경도**를 함께 반환하여 프론트에서 지도에 표시할 수 있습니다.

| 항목 | 내용 |
|------|------|
| **메서드·경로** | `GET /v1/game/{gameId}/geo/robbers-nearby` |
| **쿼리 파라미터** | `radiusMeters` (필수) — 반경(미터). 0 초과, **최대 500** |
| **인증** | Bearer 토큰 필수. **경찰만** 호출 가능 (도둑이면 404) |
| **응답** | `200`: 도둑 목록 배열 (각 항목에 playerId, distanceMeters, longitude, latitude) |

#### 요청 예시

```http
GET /v1/game/{gameId}/geo/robbers-nearby?radiusMeters=50
Authorization: Bearer <accessToken>
```

#### 응답 예시 (200 OK)

```json
[
  {
    "playerId": "player-2",
    "distanceMeters": 25.5,
    "longitude": 127.0276,
    "latitude": 37.4979
  },
  {
    "playerId": "player-4",
    "distanceMeters": 40.0,
    "longitude": 127.0280,
    "latitude": 37.4982
  }
]
```

- **playerId**: 도둑 플레이어 ID  
- **distanceMeters**: 경찰과의 거리(미터)  
- **longitude**, **latitude**: 도둑의 현재 위치 (지도 표시용)

#### 반경(radiusMeters) 지정 방식

- **호출 시마다** 쿼리 파라미터 `radiusMeters`로 지정합니다.
- 허용 범위: **0 초과, 500 이하** (미터).  
  - `GeoService.MAX_RADIUS_METERS = 500.0` 으로 상한이 정의되어 있습니다.
- 범위를 벗어나면 `400`/`404` 등 에러 응답으로 거절됩니다.

#### 에러

| 상황 | HTTP | 설명 |
|------|------|------|
| 반경이 0 이하이거나 500 초과 | 404 | `radiusMeters must be > 0 and <= 500.0` |
| 요청자가 경찰이 아님 | 404 | `Only cops can query robbers nearby` |
| 게임/플레이어 조회 실패 | 404 | 해당 리소스 없음 |

---

## 추가·수정된 파일

| 레이어 | 경로 | 설명 |
|--------|------|------|
| **domain** | `module-core/domain/.../game/RobberNearby.java` | 반경 내 도둑 한 명 (playerId, distanceMeters, longitude, latitude) |
| **application** | `module-core/application/.../game/service/GeoService.java` | 반경 검증, 경찰 여부 확인, ROBBERS+ACTIVE 필터, 거리 계산 후 반경 내만 반환 |
| **api** | `module-adaptor/inbound/api/.../game/GameGeoApi.java` | `GET .../geo/robbers-nearby` 컨트롤러 |
| **api** | `module-adaptor/inbound/api/.../game/usecase/GameGeoUseCase.java` | Geo 서비스 호출 및 응답 DTO 변환 |
| **api** | `module-adaptor/inbound/api/.../game/response/RobberNearbyResponse.java` | 응답 DTO (위도·경도 포함) |

---

## Swagger

실행 후 다음에서 확인할 수 있습니다.

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`  
- **태그**: **Game Geo** → "경찰 반경 내 도둑 조회"

---

## 동작 흐름 요약

1. 클라이언트가 `GET /v1/game/{gameId}/geo/robbers-nearby?radiusMeters=50` 호출 (Bearer 포함).
2. **GameGeoApi**에서 현재 사용자 ID를 꺼내고, **GameGeoUseCase** → **GeoService** 호출.
3. **GeoService**에서  
   - `radiusMeters` 검증 (0 초과, 500 이하),  
   - 요청자가 **경찰**인지 확인,  
   - 해당 게임의 **ROBBERS + ACTIVE** 플레이어만 대상으로  
   - **LocationStore.getDistanceMeters**(경찰, 도둑)로 거리 계산 후  
   - `distance <= radiusMeters` 인 도둑만 남기고,  
   - 각 도둑의 **LocationStore.getLocation**으로 위도·경도 조회.
4. **RobberNearby** → **RobberNearbyResponse**로 변환해 배열로 반환 (위도·경도 포함).

이 브랜치에서는 위 **경찰 반경 내 도둑 조회** 기능만 추가되어 있으며, 반경은 **API 호출 시 `radiusMeters` 쿼리 파라미터로만** 지정됩니다 (최대 500m).
