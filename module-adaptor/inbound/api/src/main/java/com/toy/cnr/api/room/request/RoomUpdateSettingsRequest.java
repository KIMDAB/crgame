package com.toy.cnr.api.room.request;

import com.toy.cnr.domain.room.*;

import java.util.List;

public record RoomUpdateSettingsRequest(
    String gameMode,
    int minPlayers,
    int maxPlayers,
    int copsCount,
    int robbersCount,
    int gameDurationMinutes,
    int escapeTimeMinutes,
    double actionRadiusMeters,
    /** 게임 장소 좌표. null 이면 서버가 기존 mapZone 유지(다른 설정만 변경). */
    MapZoneRequest mapZone
) {
    /** 집결지·활동/감옥/제한 구역 (EPSG:4326 위·경도). */
    public record MapZoneRequest(
        GeoPointRequest rallyPoint,
        List<GeoPointRequest> playArea,
        List<GeoPointRequest> prisonArea,
        List<GeoPointRequest> restrictedArea
    ) {}

    public record GeoPointRequest(double latitude, double longitude) {}

    public RoomUpdateSettingsCommand toCommand(String roomId, String requesterId) {
        MapZone zone = null;
        if (mapZone != null) {
            zone = new MapZone(
                mapZone.rallyPoint() != null
                    ? new GeoPoint(mapZone.rallyPoint().latitude(), mapZone.rallyPoint().longitude())
                    : null,
                toGeoPoints(mapZone.playArea()),
                toGeoPoints(mapZone.prisonArea()),
                toGeoPoints(mapZone.restrictedArea())
            );
        }
        return new RoomUpdateSettingsCommand(
            roomId,
            requesterId,
            new RoomSettings(
                GameMode.valueOf(gameMode != null ? gameMode : GameMode.BASIC.name()),
                minPlayers,
                maxPlayers,
                copsCount,
                robbersCount,
                gameDurationMinutes,
                escapeTimeMinutes,
                actionRadiusMeters,
                zone
            )
        );
    }

    private static List<GeoPoint> toGeoPoints(List<GeoPointRequest> requests) {
        if (requests == null) return List.of();
        return requests.stream()
            .map(p -> new GeoPoint(p.latitude(), p.longitude()))
            .toList();
    }
}
