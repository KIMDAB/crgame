package com.toy.cnr.cache.game;

import com.toy.cnr.cache.game.key.GameKey;
import com.toy.cnr.port.game.GameEventPublisher;
import com.toy.cnr.port.game.model.GameEventDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub 기반 게임 이벤트 발행 구현.
 * <p>
 * Channel: {@code game:{gameId}:events}
 * <br>
 * Redis command: PUBLISH
 */
@Component
public class GameEventRedisPublisher implements GameEventPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public GameEventRedisPublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void publish(String gameId, GameEventDto event) {
        var channel = GameKey.events(gameId);
        redisTemplate.convertAndSend(channel, event);
    }
}
