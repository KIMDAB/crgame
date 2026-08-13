package com.toy.cnr.port.room.model;

import java.util.List;

/**
 * 방 설정 DTO (Redis HASH 저장용)
 */
public record RoomSettingsDto(
    String gameMode,
    int minPlayers,
    int maxPlayers,
    int copsCount,
    int robbersCount,
    int gameDurationMinutes,
    int escapeTimeMinutes,
    double actionRadiusMeters,
    GeoPointDto rallyPoint,
    List<GeoPointDto> playArea,
    List<GeoPointDto> prisonArea,
    List<GeoPointDto> restrictedArea
) {
    public record GeoPointDto(double latitude, double longitude) {}
}
