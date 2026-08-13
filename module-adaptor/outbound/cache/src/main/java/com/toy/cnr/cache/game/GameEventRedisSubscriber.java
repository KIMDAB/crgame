package com.toy.cnr.cache.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toy.cnr.cache.game.key.GameKey;
import com.toy.cnr.port.game.GameEventSubscriber;
import com.toy.cnr.port.game.model.GameEventDto;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Redis Pub/Sub 기반 게임 이벤트 구독 구현.
 * <p>
 * 동일 게임 채널에 복수의 클라이언트(SSE 커넥션)가 독립적으로 구독할 수 있도록
 * UUID 기반 {@code subscriberId}로 각 구독을 관리합니다.
 * <p>
 * Channel: {@code game:{gameId}:events}
 */
@Component
public class GameEventRedisSubscriber implements GameEventSubscriber {

    private final RedisMessageListenerContainer listenerContainer;
    private final ObjectMapper objectMapper;
    private final Map<String, MessageListener> activeListeners = new ConcurrentHashMap<>();

    public GameEventRedisSubscriber(
        RedisMessageListenerContainer listenerContainer,
        ObjectMapper objectMapper
    ) {
        this.listenerContainer = listenerContainer;
        this.objectMapper = objectMapper;
    }

    @Override
    public String subscribe(String gameId, Consumer<GameEventDto> onMessage) {
        var subscriberId = GameKey.generateSubscriberId(gameId);
        var topic = new ChannelTopic(GameKey.events(gameId));

        MessageListener listener = (message, pattern) -> {
            try {
                var dto = objectMapper.readValue(message.getBody(), GameEventDto.class);
                onMessage.accept(dto);
            } catch (Exception e) {
                // 역직렬화 실패 시 무시
            }
        };

        activeListeners.put(subscriberId, listener);
        listenerContainer.addMessageListener(listener, topic);

        return subscriberId;
    }

    @Override
    public void unsubscribe(String subscriberId) {
        var listener = activeListeners.remove(subscriberId);
        if (listener != null) {
            listenerContainer.removeMessageListener(listener);
        }
    }
}
