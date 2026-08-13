package com.toy.cnr.cache.game;

import com.toy.cnr.cache.game.key.GameKey;
import com.toy.cnr.port.room.RoomEventPublisher;
import com.toy.cnr.port.room.model.RoomEventDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub 기반 방 이벤트 발행 구현.
 * <p>
 * Channel: {@code room:{roomId}:events}
 * <br>
 * Redis command: PUBLISH
 */
@Component
public class RoomEventRedisPublisher implements RoomEventPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public RoomEventRedisPublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void publish(String roomId, RoomEventDto event) {
        var channel = GameKey.roomEvents(roomId);
        redisTemplate.convertAndSend(channel, event);
    }
}
