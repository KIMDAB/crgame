package com.toy.cnr.api.game.response;

import com.toy.cnr.domain.game.Gem;

public record GemResponse(
    String gemId,
    double latitude,
    double longitude,
    String status,
    long spawnedAt
) {
    public static GemResponse from(Gem gem) {
        return new GemResponse(
            gem.gemId(),
            gem.latitude(),
            gem.longitude(),
            gem.status().name(),
            gem.spawnedAt()
        );
    }
}
