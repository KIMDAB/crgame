package com.toy.cnr.api.room.request;

import com.toy.cnr.domain.room.RoomCreateCommand;

public record RoomCreateRequest(String playerName) {

    public RoomCreateCommand toCommand(String playerId) {
        return new RoomCreateCommand(playerId, playerName);
    }
}
