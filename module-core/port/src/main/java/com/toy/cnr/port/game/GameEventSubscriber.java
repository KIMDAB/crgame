package com.toy.cnr.port.game;

import com.toy.cnr.port.game.model.GameEventDto;

import java.util.function.Consumer;

/**
 * 게임 이벤트 구독 포트.
 * <p>
 * 동일 게임에 복수의 클라이언트가 독립적으로 구독할 수 있도록
 * 구독 단위를 식별하는 {@code subscriberId}를 반환합니다.
 */
public interface GameEventSubscriber {

    /**
     * 게임 이벤트 채널을 구독합니다.
     *
     * @param gameId    게임 세션 ID
     * @param onMessage 이벤트 수신 시 호출되는 콜백
     * @return 구독 해제에 사용할 subscriberId
     */
    String subscribe(String gameId, Consumer<GameEventDto> onMessage);

    /**
     * 구독을 해제합니다.
     *
     * @param subscriberId {@link #subscribe}가 반환한 ID
     */
    void unsubscribe(String subscriberId);
}
