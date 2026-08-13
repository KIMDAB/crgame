package com.toy.cnr.port.game;

import com.toy.cnr.port.game.model.GameEventDto;

/**
 * 게임 이벤트 발행 포트.
 * <p>
 * 구현체는 Redis Pub/Sub 등 메시징 인프라를 사용합니다.
 */
public interface GameEventPublisher {

    /**
     * 게임 이벤트를 발행합니다.
     *
     * @param gameId 게임 세션 ID
     * @param event  발행할 이벤트 DTO
     */
    void publish(String gameId, GameEventDto event);
}
