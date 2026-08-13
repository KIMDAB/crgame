package com.toy.cnr.api.room.response;

import com.toy.cnr.domain.room.Room;

public record RoomResponse(
    String roomId,
    String hostId,
    String status,
    int playerCount,
    long createdAt
) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(
            room.roomId(),
            room.hostId(),
            room.status().name(),
            room.players().size(),
            room.createdAt()
        );
    }
}
