# CNR Game API 구현 진행상황

## 전체 Phase 현황

| Phase | 내용 | 상태 |
|-------|------|------|
| Phase 1 | Domain models (room/ + game/ 확장) | ✅ 완료 |
| Phase 2 | Port interfaces + DTOs | ✅ 완료 |
| Phase 3 | Cache layer (Redis 구현체 + GameKey) | ✅ 완료 |
| Phase 4 | Application services + Mappers | ✅ 완료 |
| Phase 5 | Room API | ✅ 완료 |
| Phase 6 | Game State API | ✅ 완료 |
| Phase 7 | Game Action API | ✅ 완료 |
| Phase 8 | GameEvent 확장 | ✅ 완료 |
| Phase 9 | Gem API | ✅ 완료 |
| Phase 10 | Port (GameTimerPort, GameRegistryStore) | ✅ 완료 |
| Phase 11 | Application (PolygonUtils, LocationService 수정, GameTimerService, GemSpawnService 확장, GameActionService 수정) | ✅ 완료 |
| Phase 12 | Cache (GameRegistryRedisStore, GameKey 수정) | ✅ 완료 |
| Phase 13 | Batch (SpringGameTimerAdapter, GemSpawnScheduler, SchedulerConfig) | ✅ 완료 |
| Phase 14 | Build config (bootstrap에 batch 추가) | ✅ 완료 |

---

## Phase 1 — Domain models

### Room domain (`module-core/domain/.../domain/room/`)

| 파일 | 상태 |
|------|------|
| `GeoPoint.java` | ⬜ |
| `GameMode.java` | ⬜ |
| `RoomStatus.java` | ⬜ |
| `MapZone.java` | ⬜ |
| `RoomSettings.java` | ⬜ |
| `RoomPlayer.java` | ⬜ |
| `Room.java` | ⬜ |
| `RoomCreateCommand.java` | ⬜ |
| `RoomUpdateSettingsCommand.java` | ⬜ |
| `RoomJoinCommand.java` | ⬜ |
| `RoomLeaveCommand.java` | ⬜ |
| `GameStartCommand.java` | ⬜ |

### Game domain 확장 (`module-core/domain/.../domain/game/`)

| 파일 | 상태 |
|------|------|
| `PlayerRole.java` | ⬜ |
| `PlayerStatus.java` | ⬜ |
| `GameStatus.java` | ⬜ |
| `GemStatus.java` | ⬜ |
| `PingType.java` | ⬜ |
| `PlayerStats.java` | ⬜ |
| `InGamePlayer.java` | ⬜ |
| `GameState.java` | ⬜ |
| `Gem.java` | ⬜ |
| `ArrestCommand.java` | ⬜ |
| `RescueCommand.java` | ⬜ |
| `CollectGemCommand.java` | ⬜ |
| `SendPingCommand.java` | ⬜ |
| `GameEvent.java` (GemCollected, GemSpawned, PingAlert 추가) | ⬜ |

---

## Phase 2 — Port interfaces + DTOs

### Room port (`module-core/port/.../port/room/`)

| 파일 | 상태 |
|------|------|
| `RoomStore.java` | ⬜ |
| `model/RoomDto.java` | ⬜ |
| `model/RoomPlayerDto.java` | ⬜ |
| `model/RoomSettingsDto.java` | ⬜ |

### Game port 확장 (`module-core/port/.../port/game/`)

| 파일 | 상태 |
|------|------|
| `GameStateStore.java` | ⬜ |
| `InGamePlayerStore.java` | ⬜ |
| `GemStore.java` | ⬜ |
| `model/GameStateDto.java` | ⬜ |
| `model/InGamePlayerDto.java` | ⬜ |
| `model/GemDto.java` | ⬜ |

---

## Phase 3 — Cache layer

| 파일 | 상태 |
|------|------|
| `cache/game/RoomRedisStore.java` | ⬜ |
| `cache/game/GameStateRedisStore.java` | ⬜ |
| `cache/game/InGamePlayerRedisStore.java` | ⬜ |
| `cache/game/GemRedisStore.java` | ⬜ |
| `cache/game/key/GameKey.java` (수정) | ⬜ |

---

## Phase 4 — Application services + Mappers

| 파일 | 상태 |
|------|------|
| `application/room/service/RoomService.java` | ⬜ |
| `application/room/mapper/RoomMapper.java` | ⬜ |
| `application/game/service/GameActionService.java` | ⬜ |
| `application/game/service/GameStateService.java` | ⬜ |
| `application/game/service/GemService.java` | ⬜ |
| `application/game/mapper/GameStateMapper.java` | ⬜ |
| `application/game/mapper/InGamePlayerMapper.java` | ⬜ |
| `application/game/mapper/GemMapper.java` | ⬜ |
| `application/game/mapper/GameEventMapper.java` (수정) | ⬜ |

---

## Phase 5 — Room API

| 파일 | 상태 |
|------|------|
| `api/room/RoomApi.java` | ⬜ |
| `api/room/RoomUseCase.java` | ⬜ |
| `api/room/request/RoomCreateRequest.java` | ⬜ |
| `api/room/request/RoomUpdateSettingsRequest.java` | ⬜ |
| `api/room/request/RoomJoinRequest.java` | ⬜ |
| `api/room/request/RoomLeaveRequest.java` | ⬜ |
| `api/room/request/GameStartRequest.java` | ⬜ |
| `api/room/response/RoomResponse.java` | ⬜ |
| `api/room/response/RoomDetailResponse.java` | ⬜ |
| `api/room/response/RoomPlayerResponse.java` | ⬜ |
| `api/room/response/RoomPlayersResponse.java` | ⬜ |

---

## Phase 6 — Game State API

| 파일 | 상태 |
|------|------|
| `api/game/GameStateApi.java` | ⬜ |
| `api/game/usecase/GameStateUseCase.java` | ⬜ |
| `api/game/response/GameStateResponse.java` | ⬜ |
| `api/game/response/InGamePlayerResponse.java` | ⬜ |
| `api/game/response/InGamePlayersResponse.java` | ⬜ |
| `api/game/response/GameResultResponse.java` | ⬜ |

---

## Phase 7 — Game Action API

| 파일 | 상태 |
|------|------|
| `api/game/GameActionApi.java` | ⬜ |
| `api/game/usecase/GameActionUseCase.java` | ⬜ |
| `api/game/request/ArrestRequest.java` | ⬜ |
| `api/game/request/RescueRequest.java` | ⬜ |
| `api/game/request/CollectGemRequest.java` | ⬜ |
| `api/game/request/SendPingRequest.java` | ⬜ |

---

## Phase 8 — GameEvent 확장

| 파일 | 상태 |
|------|------|
| `api/game/response/GameEventResponse.java` (수정) | ⬜ |
| `application/game/mapper/GameEventMapper.java` (수정) | ⬜ |

---

## Phase 9 — Gem API

| 파일 | 상태 |
|------|------|
| `api/game/GameGemApi.java` | ⬜ |
| `api/game/usecase/GameGemUseCase.java` | ⬜ |
| `api/game/response/GemResponse.java` | ⬜ |
| `api/game/response/GemsResponse.java` | ⬜ |
