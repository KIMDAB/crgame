package com.toy.cnr.cache.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toy.cnr.cache.game.key.GameKey;
import com.toy.cnr.port.game.LocationSubscriber;
import com.toy.cnr.port.game.model.LocationDto;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Redis Pub/Sub 기반 좌표 이벤트 구독 구현.
 * <p>
 * {@link RedisMessageListenerContainer}에 동적으로 {@link MessageListener}를
 * 등록/해제하여 채널 구독을 관리합니다.
 * <p>
 * Channel: {@code game:{gameId}:player:{playerId}:location}
 */
@Component
public class LocationRedisSubscriber implements LocationSubscriber {

    private final RedisMessageListenerContainer listenerContainer;
    private final ObjectMapper objectMapper;
    private final Map<String, MessageListener> activeListeners = new ConcurrentHashMap<>();

    public LocationRedisSubscriber(
        RedisMessageListenerContainer listenerContainer,
        ObjectMapper objectMapper
    ) {
        this.listenerContainer = listenerContainer;
        this.objectMapper = objectMapper;
    }

    @Override
    public void subscribe(
        String gameId,
        String playerId,
        Consumer<LocationDto> onMessage
    ) {
        var channel = GameKey.location(gameId, playerId);
        var topic = new ChannelTopic(channel);

        MessageListener listener = (message, pattern) -> {
            try {
                var dto = objectMapper.readValue(message.getBody(), LocationDto.class);
                onMessage.accept(dto);
            } catch (Exception e) {
                // 역직렬화 실패 시 무시
            }
        };

        activeListeners.put(channel, listener);
        listenerContainer.addMessageListener(listener, topic);
    }

    @Override
    public void unsubscribe(String gameId, String playerId) {
        var channel = GameKey.location(gameId, playerId);
        var listener = activeListeners.remove(channel);
        if (listener != null) {
            listenerContainer.removeMessageListener(listener);
        }
    }

}
