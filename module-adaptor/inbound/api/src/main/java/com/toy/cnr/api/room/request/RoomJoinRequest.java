package com.toy.cnr.api.room.request;

import com.toy.cnr.domain.room.RoomJoinCommand;

public record RoomJoinRequest(String playerName) {

    public RoomJoinCommand toCommand(String roomId, String playerId) {
        return new RoomJoinCommand(roomId, playerId, playerName);
    }
}
