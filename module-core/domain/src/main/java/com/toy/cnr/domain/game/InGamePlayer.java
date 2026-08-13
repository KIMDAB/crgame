package com.toy.cnr.domain.game;

/**
 * 인게임 플레이어 정보.
 *
 * @param playerId      플레이어 ID
 * @param playerName    플레이어 이름
 * @param role          역할 (경찰/도둑)
 * @param status        상태 (활성/체포됨)
 * @param stats         성과 통계
 * @param lastUpdatedAt 마지막 업데이트 시각 (epoch millis)
 */
public record InGamePlayer(
    String playerId,
    String playerName,
    PlayerRole role,
    PlayerStatus status,
    PlayerStats stats,
    long lastUpdatedAt
) {}
