package com.toy.cnr.api.room.response;

import com.toy.cnr.domain.room.RoomPlayer;

import java.util.List;

public record RoomPlayersResponse(List<RoomPlayerResponse> players) {

    public static RoomPlayersResponse from(List<RoomPlayer> players) {
        return new RoomPlayersResponse(
            players.stream().map(RoomPlayerResponse::from).toList()
        );
    }
}
