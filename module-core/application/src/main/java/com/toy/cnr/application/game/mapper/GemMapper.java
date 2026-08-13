package com.toy.cnr.application.game.mapper;

import com.toy.cnr.domain.game.Gem;
import com.toy.cnr.domain.game.GemStatus;
import com.toy.cnr.port.game.model.GemDto;

public final class GemMapper {

    private GemMapper() {}

    public static GemDto toDto(Gem gem) {
        return new GemDto(
            gem.gemId(),
            gem.latitude(),
            gem.longitude(),
            gem.status().name(),
            gem.collectedBy(),
            gem.spawnedAt()
        );
    }

    public static Gem toDomain(GemDto dto) {
        return new Gem(
            dto.gemId(),
            dto.latitude(),
            dto.longitude(),
            GemStatus.valueOf(dto.status()),
            dto.collectedBy(),
            dto.spawnedAt()
        );
    }
}
