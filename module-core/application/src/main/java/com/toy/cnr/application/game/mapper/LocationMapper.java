package com.toy.cnr.application.game.mapper;

import com.toy.cnr.domain.game.LocationPublishCommand;
import com.toy.cnr.domain.game.PlayerLocation;
import com.toy.cnr.port.game.model.LocationDto;

public final class LocationMapper {

    private LocationMapper() {}

    public static PlayerLocation toDomain(String gameId, LocationDto dto) {
        return new PlayerLocation(
            gameId,
            dto.playerId(),
            dto.longitude(),
            dto.latitude(),
            dto.timestamp()
        );
    }

    public static LocationDto toDto(LocationPublishCommand command, long timestamp) {
        return new LocationDto(
            command.playerId(),
            command.longitude(),
            command.latitude(),
            timestamp
        );
    }

    public static PlayerLocation fromCommand(LocationPublishCommand command, long timestamp) {
        return new PlayerLocation(
            command.gameId(),
            command.playerId(),
            command.longitude(),
            command.latitude(),
            timestamp
        );
    }
}
