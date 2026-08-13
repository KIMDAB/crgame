package com.toy.cnr.domain.game;

/**
 * 인게임 보석.
 *
 * @param gemId       보석 ID
 * @param latitude    위도
 * @param longitude   경도
 * @param status      보석 상태 (획득 가능 / 획득됨)
 * @param collectedBy 획득한 플레이어 ID (null if AVAILABLE)
 * @param spawnedAt   생성 시각 (epoch millis)
 */
public record Gem(
    String gemId,
    double latitude,
    double longitude,
    GemStatus status,
    String collectedBy,
    long spawnedAt
) {}
