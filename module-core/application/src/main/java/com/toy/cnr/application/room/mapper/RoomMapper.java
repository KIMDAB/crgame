package com.toy.cnr.application.room.mapper;

import com.toy.cnr.domain.room.*;
import com.toy.cnr.port.room.model.RoomDto;
import com.toy.cnr.port.room.model.RoomPlayerDto;
import com.toy.cnr.port.room.model.RoomSettingsDto;

import java.util.Collections;
import java.util.List;

public final class RoomMapper {

    private RoomMapper() {}

    public static RoomDto toDto(Room room) {
        return new RoomDto(
            room.roomId(),
            room.hostId(),
            toSettingsDto(room.settings()),
            room.status().name(),
            room.players().stream().map(RoomMapper::toPlayerDto).toList(),
            room.createdAt()
        );
    }

    public static Room toDomain(RoomDto dto) {
        return new Room(
            dto.roomId(),
            dto.hostId(),
            toSettings(dto.settings()),
            RoomStatus.valueOf(dto.status()),
            dto.players().stream().map(RoomMapper::toPlayer).toList(),
            dto.createdAt()
        );
    }

    public static RoomPlayerDto toPlayerDto(RoomPlayer player) {
        return new RoomPlayerDto(
            player.playerId(),
            player.playerName(),
            player.isHost(),
            player.joinedAt()
        );
    }

    public static RoomPlayer toPlayer(RoomPlayerDto dto) {
        return new RoomPlayer(
            dto.playerId(),
            dto.playerName(),
            dto.isHost(),
            dto.joinedAt()
        );
    }

    /** 도메인 mapZone → Redis 저장용 flat 좌표 필드(rallyPoint, playArea, …). */
    public static RoomSettingsDto toSettingsDto(RoomSettings settings) {
        if (settings == null) {
            return null;
        }
        var zone = settings.mapZone();
        return new RoomSettingsDto(
            settings.gameMode().name(),
            settings.minPlayers(),
            settings.maxPlayers(),
            settings.copsCount(),
            settings.robbersCount(),
            settings.gameDurationMinutes(),
            settings.escapeTimeMinutes(),
            settings.actionRadiusMeters(),
            zone != null && zone.rallyPoint() != null
                ? new RoomSettingsDto.GeoPointDto(zone.rallyPoint().latitude(), zone.rallyPoint().longitude())
                : null,
            zone != null && zone.playArea() != null
                ? zone.playArea().stream()
                    .map(p -> new RoomSettingsDto.GeoPointDto(p.latitude(), p.longitude()))
                    .toList()
                : null,
            zone != null && zone.prisonArea() != null
                ? zone.prisonArea().stream()
                    .map(p -> new RoomSettingsDto.GeoPointDto(p.latitude(), p.longitude()))
                    .toList()
                : null,
            zone != null && zone.restrictedArea() != null
                ? zone.restrictedArea().stream()
                    .map(p -> new RoomSettingsDto.GeoPointDto(p.latitude(), p.longitude()))
                    .toList()
                : null
        );
    }

    /** Redis settings JSON → 도메인 RoomSettings (mapZone 복원). */
    public static RoomSettings toSettings(RoomSettingsDto dto) {
        if (dto == null) {
            return RoomSettings.defaultSettings();
        }
        MapZone zone = null;
        if (hasMapZoneData(dto)) {
            zone = new MapZone(
                dto.rallyPoint() != null
                    ? new GeoPoint(dto.rallyPoint().latitude(), dto.rallyPoint().longitude())
                    : null,
                dto.playArea() != null
                    ? dto.playArea().stream()
                        .map(p -> new GeoPoint(p.latitude(), p.longitude()))
                        .toList()
                    : Collections.emptyList(),
                dto.prisonArea() != null
                    ? dto.prisonArea().stream()
                        .map(p -> new GeoPoint(p.latitude(), p.longitude()))
                        .toList()
                    : Collections.emptyList(),
                dto.restrictedArea() != null
                    ? dto.restrictedArea().stream()
                        .map(p -> new GeoPoint(p.latitude(), p.longitude()))
                        .toList()
                    : Collections.emptyList()
            );
        }
        return new RoomSettings(
            GameMode.valueOf(dto.gameMode()),
            dto.minPlayers(),
            dto.maxPlayers(),
            dto.copsCount(),
            dto.robbersCount(),
            dto.gameDurationMinutes(),
            dto.escapeTimeMinutes(),
            dto.actionRadiusMeters(),
            zone
        );
    }

    /** 좌표 필드 중 하나라도 있으면 mapZone 으로 조립. */
    private static boolean hasMapZoneData(RoomSettingsDto dto) {
        return dto.rallyPoint() != null
            || hasGeoPoints(dto.playArea())
            || hasGeoPoints(dto.prisonArea())
            || hasGeoPoints(dto.restrictedArea());
    }

    private static boolean hasGeoPoints(List<RoomSettingsDto.GeoPointDto> points) {
        return points != null && !points.isEmpty();
    }
}
