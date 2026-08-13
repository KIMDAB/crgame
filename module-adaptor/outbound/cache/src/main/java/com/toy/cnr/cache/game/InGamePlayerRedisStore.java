package com.toy.cnr.cache.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toy.cnr.cache.game.key.GameKey;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.InGamePlayerStore;
import com.toy.cnr.port.game.model.InGamePlayerDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Redis HASH 기반 인게임 플레이어 저장소 구현.
 * <p>
 * Redis key: {@code game:{gameId}:players}  (HASH field: playerId → JSON)
 */
@Repository
public class InGamePlayerRedisStore implements InGamePlayerStore {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public InGamePlayerRedisStore(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public RepositoryResult<Void> savePlayer(String gameId, InGamePlayerDto player) {
        try {
            var key = GameKey.gamePlayers(gameId);
            redisTemplate.opsForHash().put(key, player.playerId(), objectMapper.writeValueAsString(player));
            return new RepositoryResult.Found<>(null);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<InGamePlayerDto> getPlayer(String gameId, String playerId) {
        try {
            var key = GameKey.gamePlayers(gameId);
            var value = redisTemplate.opsForHash().get(key, playerId);
            if (value == null) {
                return new RepositoryResult.NotFound<>("Player not found: " + playerId + " in game: " + gameId);
            }
            return new RepositoryResult.Found<>(objectMapper.readValue(value.toString(), InGamePlayerDto.class));
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<List<InGamePlayerDto>> getAllPlayers(String gameId) {
        try {
            var key = GameKey.gamePlayers(gameId);
            var values = redisTemplate.opsForHash().values(key);
            var players = values.stream()
                .map(v -> {
                    try {
                        return objectMapper.readValue(v.toString(), InGamePlayerDto.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
            return new RepositoryResult.Found<>(players);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<Void> updatePlayer(String gameId, InGamePlayerDto player) {
        return savePlayer(gameId, player);
    }
}
