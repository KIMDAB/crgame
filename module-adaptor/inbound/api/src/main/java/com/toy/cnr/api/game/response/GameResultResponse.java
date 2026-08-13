package com.toy.cnr.api.game.response;

import com.toy.cnr.domain.game.GameState;
import com.toy.cnr.domain.game.InGamePlayer;

import java.util.List;

public record GameResultResponse(
    String gameId,
    String status,
    List<InGamePlayerResponse> players
) {
    public static GameResultResponse from(GameState state, List<InGamePlayer> players) {
        return new GameResultResponse(
            state.gameId(),
            state.status().name(),
            players.stream().map(InGamePlayerResponse::from).toList()
        );
    }
}
