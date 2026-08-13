package com.toy.cnr.api.room.request;

import com.toy.cnr.domain.room.RoomLeaveCommand;

public record RoomLeaveRequest() {

    public RoomLeaveCommand toCommand(String roomId, String playerId) {
        return new RoomLeaveCommand(roomId, playerId);
    }
}
