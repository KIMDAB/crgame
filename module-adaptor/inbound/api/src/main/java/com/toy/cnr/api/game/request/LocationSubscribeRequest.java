package com.toy.cnr.api.game.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "좌표 구독 요청")
public record LocationSubscribeRequest(
    @Schema(description = "게임 세션 ID", example = "game-1")
    String gameId,

    @Schema(description = "구독할 플레이어 ID 목록", example = "[\"player-1\", \"player-2\"]")
    List<String> playerIds
) {
}
