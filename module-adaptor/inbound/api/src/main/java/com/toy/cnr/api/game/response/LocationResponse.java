package com.toy.cnr.api.game.response;

import com.toy.cnr.domain.game.PlayerLocation;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌표 응답")
public record LocationResponse(
    @Schema(description = "플레이어 ID", example = "player-1")
    String playerId,

    @Schema(description = "경도", example = "127.0276")
    double longitude,

    @Schema(description = "위도", example = "37.4979")
    double latitude,

    @Schema(description = "좌표 갱신 시각 (epoch millis)", example = "1707350400000")
    long timestamp
) {
    public static LocationResponse from(PlayerLocation location) {
        return new LocationResponse(
            location.playerId(),
            location.longitude(),
            location.latitude(),
            location.timestamp()
        );
    }
}
