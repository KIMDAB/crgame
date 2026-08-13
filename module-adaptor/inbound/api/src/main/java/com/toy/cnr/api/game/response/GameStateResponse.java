package com.toy.cnr.api.game.response;

import com.toy.cnr.domain.game.GameState;

public record GameStateResponse(
    String gameId,
    String roomId,
    String status,
    long startedAt,
    long endsAt
) {
    public static GameStateResponse from(GameState state) {
        return new GameStateResponse(
            state.gameId(),
            state.roomId(),
            state.status().name(),
            state.startedAt(),
            state.endsAt()
        );
    }
}
