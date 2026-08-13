package com.toy.cnr.api.game.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게임 이벤트 구독 요청")
public record GameEventSubscribeRequest(
    @Schema(description = "게임 세션 ID", example = "game-1")
    String gameId,

    @Schema(description = "구독하는 플레이어 ID", example = "player-1")
    String playerId
) {}
