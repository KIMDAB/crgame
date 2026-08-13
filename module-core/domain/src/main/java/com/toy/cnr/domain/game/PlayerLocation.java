package com.toy.cnr.domain.game;

/**
 * 플레이어 좌표 도메인 모델.
 *
 * @param gameId    게임 세션 ID
 * @param playerId  플레이어 ID
 * @param longitude 경도
 * @param latitude  위도
 * @param timestamp 좌표 갱신 시각 (epoch millis)
 */
public record PlayerLocation(
    String gameId,
    String playerId,
    double longitude,
    double latitude,
    long timestamp
) {
}
