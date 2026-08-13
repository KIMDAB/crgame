package com.toy.cnr.domain.room;

/** 방 설정 변경 커맨드 (방장만 가능) */
public record RoomUpdateSettingsCommand(
    String roomId,
    String requesterId,
    RoomSettings settings
) {}
