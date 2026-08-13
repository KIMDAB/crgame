package com.toy.cnr.application.room.mapper;

import com.toy.cnr.domain.room.RoomEvent;
import com.toy.cnr.port.room.model.RoomEventDto;
import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class RoomEventMapper {

    /** RoomEvent(domain) → RoomEventDto(port) — 발행 경로 */
    public static RoomEventDto toDto(RoomEvent event) {
        return switch (event) {
            case RoomEvent.PlayerJoined e -> new RoomEventDto(
                e.roomId(), "PLAYER_JOINED",
                Map.of("playerId", e.playerId(), "playerName", e.playerName()),
                e.timestamp()
            );
            case RoomEvent.PlayerLeft e -> new RoomEventDto(
                e.roomId(), "PLAYER_LEFT",
                Map.of("playerId", e.playerId()),
                e.timestamp()
            );
        };
    }

    /** RoomEventDto(port) → RoomEvent(domain) — 구독 경로 */
    public static RoomEvent toDomain(RoomEventDto dto) {
        return switch (dto.type()) {
            case "PLAYER_JOINED" -> new RoomEvent.PlayerJoined(
                dto.roomId(),
                dto.data().get("playerId"),
                dto.data().get("playerName"),
                dto.timestamp()
            );
            case "PLAYER_LEFT" -> new RoomEvent.PlayerLeft(
                dto.roomId(),
                dto.data().get("playerId"),
                dto.timestamp()
            );
            default -> throw new IllegalArgumentException("Unknown room event type: " + dto.type());
        };
    }
}
