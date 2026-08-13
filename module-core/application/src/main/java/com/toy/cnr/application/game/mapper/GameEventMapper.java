package com.toy.cnr.application.game.mapper;

import com.toy.cnr.domain.game.GameEvent;
import com.toy.cnr.port.game.model.GameEventDto;

import java.util.Map;

public final class GameEventMapper {

    private GameEventMapper() {}

    /** GameEvent(domain) → GameEventDto(port) — 발행 경로 */
    public static GameEventDto toDto(GameEvent event) {
        return switch (event) {
            case GameEvent.PlayerArrested e -> new GameEventDto(
                e.gameId(), "PLAYER_ARRESTED",
                Map.of("copsId", e.copsId(), "robberId", e.robberId()),
                e.timestamp()
            );
            case GameEvent.PlayerRescued e -> new GameEventDto(
                e.gameId(), "PLAYER_RESCUED",
                Map.of("rescuerId", e.rescuerId(), "rescuedId", e.rescuedId()),
                e.timestamp()
            );
            case GameEvent.PrisonEscapeWarning e -> new GameEventDto(
                e.gameId(), "PRISON_ESCAPE_WARNING",
                Map.of("playerId", e.playerId()),
                e.timestamp()
            );
            case GameEvent.RestrictedAreaEntered e -> new GameEventDto(
                e.gameId(), "RESTRICTED_AREA_ENTERED",
                Map.of(
                    "playerId", e.playerId(),
                    "latitude", String.valueOf(e.latitude()),
                    "longitude", String.valueOf(e.longitude())
                ),
                e.timestamp()
            );
            case GameEvent.Announcement e -> new GameEventDto(
                e.gameId(), "ANNOUNCEMENT",
                Map.of("senderId", e.senderId(), "message", e.message()),
                e.timestamp()
            );
            case GameEvent.GameStarted e -> new GameEventDto(
                e.gameId(), "GAME_STARTED",
                Map.of(),
                e.timestamp()
            );
            case GameEvent.GameEnded e -> new GameEventDto(
                e.gameId(), "GAME_ENDED",
                Map.of("winnerRole", e.winnerRole()),
                e.timestamp()
            );
            case GameEvent.RoleAssigned e -> new GameEventDto(
                e.gameId(), "ROLE_ASSIGNED",
                Map.of("playerId", e.playerId(), "role", e.role()),
                e.timestamp()
            );
            case GameEvent.GemCollected e -> new GameEventDto(
                e.gameId(), "GEM_COLLECTED",
                Map.of("robberId", e.robberId(), "gemId", e.gemId()),
                e.timestamp()
            );
            case GameEvent.GemSpawned e -> new GameEventDto(
                e.gameId(), "GEM_SPAWNED",
                Map.of(
                    "gemId", e.gemId(),
                    "latitude", String.valueOf(e.latitude()),
                    "longitude", String.valueOf(e.longitude())
                ),
                e.timestamp()
            );
            case GameEvent.PingAlert e -> new GameEventDto(
                e.gameId(), "PING_ALERT",
                Map.of(
                    "senderId", e.senderId(),
                    "pingType", e.pingType(),
                    "latitude", String.valueOf(e.latitude()),
                    "longitude", String.valueOf(e.longitude())
                ),
                e.timestamp()
            );
        };
    }

    /** GameEventDto(port) → GameEvent(domain) — 구독 경로 */
    public static GameEvent toDomain(GameEventDto dto) {
        return switch (dto.type()) {
            case "PLAYER_ARRESTED" -> new GameEvent.PlayerArrested(
                dto.gameId(),
                dto.data().get("copsId"),
                dto.data().get("robberId"),
                dto.timestamp()
            );
            case "PLAYER_RESCUED" -> new GameEvent.PlayerRescued(
                dto.gameId(),
                dto.data().get("rescuerId"),
                dto.data().get("rescuedId"),
                dto.timestamp()
            );
            case "PRISON_ESCAPE_WARNING" -> new GameEvent.PrisonEscapeWarning(
                dto.gameId(),
                dto.data().get("playerId"),
                dto.timestamp()
            );
            case "RESTRICTED_AREA_ENTERED" -> new GameEvent.RestrictedAreaEntered(
                dto.gameId(),
                dto.data().get("playerId"),
                Double.parseDouble(dto.data().get("latitude")),
                Double.parseDouble(dto.data().get("longitude")),
                dto.timestamp()
            );
            case "ANNOUNCEMENT" -> new GameEvent.Announcement(
                dto.gameId(),
                dto.data().get("senderId"),
                dto.data().get("message"),
                dto.timestamp()
            );
            case "GAME_STARTED" -> new GameEvent.GameStarted(
                dto.gameId(),
                dto.timestamp()
            );
            case "GAME_ENDED" -> new GameEvent.GameEnded(
                dto.gameId(),
                dto.data().get("winnerRole"),
                dto.timestamp()
            );
            case "ROLE_ASSIGNED" -> new GameEvent.RoleAssigned(
                dto.gameId(),
                dto.data().get("playerId"),
                dto.data().get("role"),
                dto.timestamp()
            );
            case "GEM_COLLECTED" -> new GameEvent.GemCollected(
                dto.gameId(),
                dto.data().get("robberId"),
                dto.data().get("gemId"),
                dto.timestamp()
            );
            case "GEM_SPAWNED" -> new GameEvent.GemSpawned(
                dto.gameId(),
                dto.data().get("gemId"),
                Double.parseDouble(dto.data().get("latitude")),
                Double.parseDouble(dto.data().get("longitude")),
                dto.timestamp()
            );
            case "PING_ALERT" -> new GameEvent.PingAlert(
                dto.gameId(),
                dto.data().get("senderId"),
                dto.data().get("pingType"),
                Double.parseDouble(dto.data().get("latitude")),
                Double.parseDouble(dto.data().get("longitude")),
                dto.timestamp()
            );
            default -> throw new IllegalArgumentException("Unknown game event type: " + dto.type());
        };
    }
}
