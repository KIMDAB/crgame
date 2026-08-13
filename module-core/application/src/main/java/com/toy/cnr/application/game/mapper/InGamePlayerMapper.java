package com.toy.cnr.application.game.mapper;

import com.toy.cnr.domain.game.*;
import com.toy.cnr.port.game.model.InGamePlayerDto;

public final class InGamePlayerMapper {

    private InGamePlayerMapper() {}

    public static InGamePlayerDto toDto(InGamePlayer player) {
        return new InGamePlayerDto(
            player.playerId(),
            player.playerName(),
            player.role().name(),
            player.status().name(),
            player.stats().arrestCount(),
            player.stats().gemsCollected(),
            player.stats().rescueCount(),
            player.stats().escapeCount(),
            player.lastUpdatedAt()
        );
    }

    public static InGamePlayer toDomain(InGamePlayerDto dto) {
        return new InGamePlayer(
            dto.playerId(),
            dto.playerName(),
            PlayerRole.valueOf(dto.role()),
            PlayerStatus.valueOf(dto.status()),
            new PlayerStats(dto.arrestCount(), dto.gemsCollected(), dto.rescueCount(), dto.escapeCount()),
            dto.lastUpdatedAt()
        );
    }
}
