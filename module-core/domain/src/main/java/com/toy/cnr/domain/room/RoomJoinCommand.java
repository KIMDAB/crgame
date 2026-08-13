package com.toy.cnr.domain.room;

/** 방 참가 커맨드 */
public record RoomJoinCommand(
    String roomId,
    String playerId,
    String playerName
) {}
