package com.toy.cnr.api.game.response;

import com.toy.cnr.domain.game.RobberNearby;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "경찰 반경 내 도둑 한 명")
public record RobberNearbyResponse(
    @Schema(description = "플레이어(도둑) ID", example = "player-2")
    String playerId,

    @Schema(description = "경찰과의 거리 (미터)", example = "25.5")
    double distanceMeters,

    @Schema(description = "경도", example = "127.0276")
    double longitude,

    @Schema(description = "위도", example = "37.4979")
    double latitude
) {
    public static RobberNearbyResponse from(RobberNearby robber) {
        return new RobberNearbyResponse(
            robber.playerId(),
            robber.distanceMeters(),
            robber.longitude(),
            robber.latitude()
        );
    }
}
