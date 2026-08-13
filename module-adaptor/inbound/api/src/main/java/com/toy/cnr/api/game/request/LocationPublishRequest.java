package com.toy.cnr.api.game.request;

import com.toy.cnr.domain.game.LocationPublishCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌표 발행 요청")
public record LocationPublishRequest(
    @Schema(description = "게임 세션 ID", example = "game-1")
    String gameId,

    @Schema(description = "플레이어 ID", example = "player-1")
    String playerId,

    @Schema(description = "경도", example = "127.0276")
    double longitude,

    @Schema(description = "위도", example = "37.4979")
    double latitude
) {
    public LocationPublishCommand toCommand() {
        return new LocationPublishCommand(gameId, playerId, longitude, latitude);
    }
}
