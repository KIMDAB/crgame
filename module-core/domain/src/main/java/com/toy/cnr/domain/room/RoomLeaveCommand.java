package com.toy.cnr.domain.room;

/** 방 나가기 커맨드 */
public record RoomLeaveCommand(
    String roomId,
    String playerId
) {}
