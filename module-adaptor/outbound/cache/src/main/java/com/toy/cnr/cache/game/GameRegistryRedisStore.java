package com.toy.cnr.cache.game;

import com.toy.cnr.cache.game.key.GameKey;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.GameRegistryStore;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis SET 기반 활성 게임 레지스트리 구현.
 * <p>
 * Redis key: {@code game:active}  (SET — 진행 중인 gameId 목록)
 */
@Repository
public class GameRegistryRedisStore implements GameRegistryStore {

    private final RedisTemplate<String, Object> redisTemplate;

    public GameRegistryRedisStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RepositoryResult<Void> register(String gameId) {
        try {
            redisTemplate.opsForSet().add(GameKey.activeGames(), gameId);
            return new RepositoryResult.Found<>(null);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<Void> unregister(String gameId) {
        try {
            redisTemplate.opsForSet().remove(GameKey.activeGames(), gameId);
            return new RepositoryResult.Found<>(null);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<Set<String>> getActiveGameIds() {
        try {
            var members = redisTemplate.opsForSet().members(GameKey.activeGames());
            if (members == null) {
                return new RepositoryResult.Found<>(Set.of());
            }
            Set<String> ids = members.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
            return new RepositoryResult.Found<>(ids);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }
}
