package com.toy.cnr.port.game;

import com.toy.cnr.port.game.model.LocationDto;

/**
 * 좌표 이벤트 발행 포트 인터페이스.
 * <p>
 * Redis Pub/Sub을 통해 좌표 변경 이벤트를 발행합니다.
 */
public interface LocationPublisher {

    /**
     * 특정 게임의 플레이어 좌표 이벤트를 발행합니다.
     *
     * @param gameId   게임 세션 ID
     * @param playerId 플레이어 ID
     * @param location 좌표 데이터
     */
    void publish(String gameId, String playerId, LocationDto location);
}
