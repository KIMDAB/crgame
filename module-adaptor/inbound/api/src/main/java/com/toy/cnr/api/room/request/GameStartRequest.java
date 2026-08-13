package com.toy.cnr.api.room.request;

import com.toy.cnr.domain.room.GameStartCommand;

public record GameStartRequest() {

    public GameStartCommand toCommand(String roomId, String requesterId) {
        return new GameStartCommand(roomId, requesterId);
    }
}
