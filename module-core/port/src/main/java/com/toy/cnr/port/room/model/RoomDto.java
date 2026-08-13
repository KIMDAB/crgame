package com.toy.cnr.port.room.model;

import java.util.List;

/** 방 DTO (Redis HASH 저장용) */
public record RoomDto(
    String roomId,
    String hostId,
    RoomSettingsDto settings,
    String status,
    List<RoomPlayerDto> players,
    long createdAt
) {}
