package com.toy.cnr.api.room.response;

import com.toy.cnr.domain.room.MapZone;
import com.toy.cnr.domain.room.Room;
import com.toy.cnr.domain.room.RoomSettings;

import java.util.List;

/**
 * GET /v1/rooms/{roomId} 응답.
 * 게임 장소는 settings.mapZone (PUT /settings 로 저장된 좌표).
 */
public record RoomDetailResponse(
    String roomId,
    String hostId,
    String status,
    SettingsResponse settings,
    List<RoomPlayerResponse> players,
    long createdAt
) {
    public record SettingsResponse(
        String gameMode,
        int minPlayers,
        int maxPlayers,
        int copsCount,
        int robbersCount,
        int gameDurationMinutes,
        int escapeTimeMinutes,
        double actionRadiusMeters,
        /** 게임 장소(집결지·활동/감옥/제한 구역). 미저장 시 null. */
        MapZoneResponse mapZone
    ) {
        /** 네이버 맵 등에서 그린 구역 — 위·경도만 저장 (주소 문자열은 별도 API). */
        public record MapZoneResponse(
            GeoPointResponse rallyPoint,
            List<GeoPointResponse> playArea,
            List<GeoPointResponse> prisonArea,
            List<GeoPointResponse> restrictedArea
        ) {
            public static MapZoneResponse from(MapZone zone) {
                if (zone == null) return null;
                return new MapZoneResponse(
                    GeoPointResponse.from(zone.rallyPoint()),
                    GeoPointResponse.fromList(zone.playArea()),
                    GeoPointResponse.fromList(zone.prisonArea()),
                    GeoPointResponse.fromList(zone.restrictedArea())
                );
            }
        }

        public record GeoPointResponse(double latitude, double longitude) {
            public static GeoPointResponse from(com.toy.cnr.domain.room.GeoPoint point) {
                if (point == null) return null;
                return new GeoPointResponse(point.latitude(), point.longitude());
            }

            public static List<GeoPointResponse> fromList(List<com.toy.cnr.domain.room.GeoPoint> points) {
                if (points == null || points.isEmpty()) return null;
                return points.stream().map(GeoPointResponse::from).toList();
            }
        }

        public static SettingsResponse from(RoomSettings settings) {
            if (settings == null) return null;
            return new SettingsResponse(
                settings.gameMode().name(),
                settings.minPlayers(),
                settings.maxPlayers(),
                settings.copsCount(),
                settings.robbersCount(),
                settings.gameDurationMinutes(),
                settings.escapeTimeMinutes(),
                settings.actionRadiusMeters(),
                MapZoneResponse.from(settings.mapZone())
            );
        }
    }

    public static RoomDetailResponse from(Room room) {
        return new RoomDetailResponse(
            room.roomId(),
            room.hostId(),
            room.status().name(),
            SettingsResponse.from(room.settings()),
            room.players().stream().map(RoomPlayerResponse::from).toList(),
            room.createdAt()
        );
    }
}
