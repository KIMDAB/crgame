package com.toy.cnr.domain.room;

/** 방 생성 커맨드 */
public record RoomCreateCommand(
    String hostId,
    String hostName
) {}
