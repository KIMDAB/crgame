package com.toy.cnr.api.room.response;

import com.toy.cnr.domain.room.RoomEvent;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "방 이벤트 응답")
public record RoomEventResponse(
    @Schema(description = "방 ID", example = "ABCD")
    String roomId,

    @Schema(
        description = "이벤트 타입",
        example = "PLAYER_JOINED",
        allowableValues = {
            "PLAYER_JOINED",
            "PLAYER_LEFT"
        }
    )
    String type,

    @Schema(description = "이벤트 타입별 페이로드")
    Map<String, String> data,

    @Schema(description = "이벤트 발생 시각 (epoch millis)", example = "1707350400000")
    long timestamp
) {
    public static RoomEventResponse from(RoomEvent event) {
        return switch (event) {
            case RoomEvent.PlayerJoined e -> new RoomEventResponse(
                e.roomId(), "PLAYER_JOINED",
                Map.of("playerId", e.playerId(), "playerName", e.playerName()),
                e.timestamp()
            );
            case RoomEvent.PlayerLeft e -> new RoomEventResponse(
                e.roomId(), "PLAYER_LEFT",
                Map.of("playerId", e.playerId()),
                e.timestamp()
            );
        };
    }
}
