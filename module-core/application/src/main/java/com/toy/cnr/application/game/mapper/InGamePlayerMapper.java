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

    /** arrestedBy 없이 변환 — ACTIVE 플레이어 또는 원장 조회 전 단계에서 사용 */
    public static InGamePlayer toDomain(InGamePlayerDto dto) {
        return toDomain(dto, null);
    }

    /** arrestedBy 를 원장에서 주입받아 변환 */
    public static InGamePlayer toDomain(InGamePlayerDto dto, String arrestedBy) {
        return new InGamePlayer(
            dto.playerId(),
            dto.playerName(),
            PlayerRole.valueOf(dto.role()),
            PlayerStatus.valueOf(dto.status()),
            arrestedBy,
            new PlayerStats(dto.arrestCount(), dto.gemsCollected(), dto.rescueCount(), dto.escapeCount()),
            dto.lastUpdatedAt()
        );
    }
}
