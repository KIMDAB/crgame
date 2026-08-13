package com.toy.cnr.api.game.response;

import com.toy.cnr.domain.game.InGamePlayer;

public record InGamePlayerResponse(
    String playerId,
    String playerName,
    String role,
    String status,
    int arrestCount,
    int gemsCollected,
    int rescueCount,
    int escapeCount,
    long lastUpdatedAt
) {
    public static InGamePlayerResponse from(InGamePlayer player) {
        return new InGamePlayerResponse(
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
}
