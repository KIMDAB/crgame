package com.toy.cnr.api.room.response;

import com.toy.cnr.domain.room.RoomPlayer;

public record RoomPlayerResponse(
    String playerId,
    String playerName,
    boolean isHost,
    long joinedAt
) {
    public static RoomPlayerResponse from(RoomPlayer player) {
        return new RoomPlayerResponse(
            player.playerId(),
            player.playerName(),
            player.isHost(),
            player.joinedAt()
        );
    }
}
