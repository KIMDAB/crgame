package com.toy.cnr.domain.game;

/**
 * 좌표 발행 커맨드.
 *
 * @param gameId    게임 세션 ID
 * @param playerId  플레이어 ID
 * @param longitude 경도
 * @param latitude  위도
 */
public record LocationPublishCommand(
    String gameId,
    String playerId,
    double longitude,
    double latitude
) {
}
