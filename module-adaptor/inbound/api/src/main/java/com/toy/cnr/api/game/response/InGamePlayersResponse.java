package com.toy.cnr.api.game.response;

import com.toy.cnr.domain.game.InGamePlayer;

import java.util.List;

public record InGamePlayersResponse(List<InGamePlayerResponse> players) {

    public static InGamePlayersResponse from(List<InGamePlayer> players) {
        return new InGamePlayersResponse(
            players.stream().map(InGamePlayerResponse::from).toList()
        );
    }
}
