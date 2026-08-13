package com.toy.cnr.port.game.model;

import com.toy.cnr.port.room.model.RoomSettingsDto;

/** 게임 상태 DTO (Redis HASH 저장용) */
public record GameStateDto(
    String gameId,
    String roomId,
    String status,
    RoomSettingsDto settings,
    long startedAt,
    long endsAt
) {}
