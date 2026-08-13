package com.toy.cnr.port.game.model;

/**
 * 좌표 데이터 전달 객체.
 *
 * @param playerId  플레이어 ID
 * @param longitude 경도
 * @param latitude  위도
 * @param timestamp 좌표 갱신 시각 (epoch millis)
 */
public record LocationDto(
    String playerId,
    double longitude,
    double latitude,
    long timestamp
) {
}
