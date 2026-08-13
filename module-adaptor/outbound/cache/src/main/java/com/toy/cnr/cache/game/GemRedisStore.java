package com.toy.cnr.cache.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toy.cnr.cache.game.key.GameKey;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.GemStore;
import com.toy.cnr.port.game.model.GemDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Redis HASH 기반 보석 저장소 구현.
 * <p>
 * Redis key: {@code game:{gameId}:gems}  (HASH field: gemId → JSON)
 */
@Repository
public class GemRedisStore implements GemStore {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public GemRedisStore(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public RepositoryResult<Void> saveGem(String gameId, GemDto gem) {
        try {
            var key = GameKey.gameGems(gameId);
            redisTemplate.opsForHash().put(key, gem.gemId(), objectMapper.writeValueAsString(gem));
            return new RepositoryResult.Found<>(null);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<GemDto> getGem(String gameId, String gemId) {
        try {
            var key = GameKey.gameGems(gameId);
            var value = redisTemplate.opsForHash().get(key, gemId);
            if (value == null) {
                return new RepositoryResult.NotFound<>("Gem not found: " + gemId + " in game: " + gameId);
            }
            return new RepositoryResult.Found<>(objectMapper.readValue(value.toString(), GemDto.class));
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<List<GemDto>> getAllGems(String gameId) {
        try {
            var key = GameKey.gameGems(gameId);
            var values = redisTemplate.opsForHash().values(key);
            var gems = values.stream()
                .map(v -> {
                    try {
                        return objectMapper.readValue(v.toString(), GemDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
            return new RepositoryResult.Found<>(gems);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<Void> updateGem(String gameId, GemDto gem) {
        return saveGem(gameId, gem);
    }
}
