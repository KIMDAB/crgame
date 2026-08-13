package com.toy.cnr.application.game.service;

import com.toy.cnr.application.game.mapper.GameEventMapper;
import com.toy.cnr.domain.game.GameEvent;
import com.toy.cnr.port.game.GameEventPublisher;
import com.toy.cnr.port.game.GameEventSubscriber;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * 게임 이벤트 발행/구독 서비스.
 * <p>
 * GameEventPublisher(Pub/Sub PUBLISH),
 * GameEventSubscriber(Pub/Sub SUBSCRIBE) 포트를 사용합니다.
 */
@Service
public class GameEventService {

    private final GameEventPublisher gameEventPublisher;
    private final GameEventSubscriber gameEventSubscriber;

    public GameEventService(
        GameEventPublisher gameEventPublisher,
        GameEventSubscriber gameEventSubscriber
    ) {
        this.gameEventPublisher = gameEventPublisher;
        this.gameEventSubscriber = gameEventSubscriber;
    }

    /**
     * 게임 이벤트를 Pub/Sub으로 발행합니다.
     */
    public void publish(GameEvent event) {
        gameEventPublisher.publish(event.gameId(), GameEventMapper.toDto(event));
    }

    /**
     * 게임 이벤트 채널을 구독합니다.
     *
     * @param gameId  게임 세션 ID
     * @param onEvent 이벤트 수신 시 호출되는 콜백 (도메인 모델 전달)
     * @return 구독 해제에 사용할 subscriberId
     */
    public String subscribe(String gameId, Consumer<GameEvent> onEvent) {
        return gameEventSubscriber.subscribe(gameId, dto ->
            onEvent.accept(GameEventMapper.toDomain(dto))
        );
    }

    /**
     * 구독을 해제합니다.
     */
    public void unsubscribe(String subscriberId) {
        gameEventSubscriber.unsubscribe(subscriberId);
    }
}
