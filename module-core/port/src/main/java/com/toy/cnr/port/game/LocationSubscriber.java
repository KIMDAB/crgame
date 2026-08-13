package com.toy.cnr.port.game;

import com.toy.cnr.port.game.model.LocationDto;

import java.util.function.Consumer;

/**
 * 좌표 이벤트 구독 포트 인터페이스.
 * <p>
 * Redis Pub/Sub 채널을 구독하여 좌표 변경 이벤트를 수신합니다.
 * Consumer 콜백 패턴으로 메시지를 전달합니다.
 */
public interface LocationSubscriber {

    /**
     * 특정 게임의 플레이어 좌표 채널을 구독합니다.
     *
     * @param gameId     게임 세션 ID
     * @param playerId   구독할 플레이어 ID
     * @param onMessage  좌표 수신 시 호출되는 콜백
     */
    void subscribe(String gameId, String playerId, Consumer<LocationDto> onMessage);

    /**
     * 특정 게임의 플레이어 좌표 채널 구독을 해제합니다.
     *
     * @param gameId   게임 세션 ID
     * @param playerId 구독 해제할 플레이어 ID
     */
    void unsubscribe(String gameId, String playerId);
}
