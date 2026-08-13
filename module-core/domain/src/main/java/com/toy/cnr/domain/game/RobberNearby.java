package com.toy.cnr.domain.game;

/**
 * 경찰 기준 반경 내에 있는 도둑 한 명의 정보.
 *
 * @param playerId       플레이어(도둑) ID
 * @param distanceMeters 경찰과의 거리 (미터)
 * @param longitude      도둑 경도
 * @param latitude       도둑 위도
 */
public record RobberNearby(
    String playerId,
    double distanceMeters,
    double longitude,
    double latitude
) {}
