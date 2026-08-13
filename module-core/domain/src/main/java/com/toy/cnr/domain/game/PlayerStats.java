package com.toy.cnr.domain.game;

/**
 * 인게임 플레이어 성과 통계.
 *
 * @param arrestCount    체포한 도둑 수 (경찰)
 * @param gemsCollected  획득한 보석 수 (도둑)
 * @param rescueCount    구출한 동료 수 (도둑)
 * @param escapeCount    탈옥 횟수 (도둑)
 */
public record PlayerStats(
    int arrestCount,
    int gemsCollected,
    int rescueCount,
    int escapeCount
) {
    public static PlayerStats empty() {
        return new PlayerStats(0, 0, 0, 0);
    }

    public PlayerStats withArrestCount(int count) {
        return new PlayerStats(count, gemsCollected, rescueCount, escapeCount);
    }

    public PlayerStats withGemsCollected(int count) {
        return new PlayerStats(arrestCount, count, rescueCount, escapeCount);
    }

    public PlayerStats withRescueCount(int count) {
        return new PlayerStats(arrestCount, gemsCollected, count, escapeCount);
    }

    public PlayerStats withEscapeCount(int count) {
        return new PlayerStats(arrestCount, gemsCollected, rescueCount, count);
    }
}
