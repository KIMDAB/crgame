package com.toy.cnr.cache.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toy.cnr.cache.game.key.GameKey;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.GameStateStore;
import com.toy.cnr.port.game.model.GameStateDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Redis HASH 기반 게임 상태 저장소 구현.
 * <p>
 * Redis key: {@code game:{gameId}:state}
 */
@Repository
public class GameStateRedisStore implements GameStateStore {

    private static final String DATA_FIELD = "data";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public GameStateRedisStore(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public RepositoryResult<Void> saveGameState(GameStateDto gameState) {
        try {
            var key = GameKey.gameState(gameState.gameId());
            redisTemplate.opsForHash().put(key, DATA_FIELD, objectMapper.writeValueAsString(gameState));
            return new RepositoryResult.Found<>(null);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<GameStateDto> getGameState(String gameId) {
        try {
            var key = GameKey.gameState(gameId);
            var value = redisTemplate.opsForHash().get(key, DATA_FIELD);
            if (value == null) {
                return new RepositoryResult.NotFound<>("GameState not found: " + gameId);
            }
            return new RepositoryResult.Found<>(objectMapper.readValue(value.toString(), GameStateDto.class));
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<Void> updateStatus(String gameId, String status) {
        try {
            var result = getGameState(gameId);
            if (result instanceof RepositoryResult.NotFound<GameStateDto> notFound) {
                return new RepositoryResult.NotFound<>(notFound.message());
            }
            if (result instanceof RepositoryResult.Error<GameStateDto> error) {
                return new RepositoryResult.Error<>(error.t());
            }
            var state = ((RepositoryResult.Found<GameStateDto>) result).data();
            var updated = new GameStateDto(
                state.gameId(),
                state.roomId(),
                status,
                state.settings(),
                state.startedAt(),
                state.endsAt()
            );
            return saveGameState(updated);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }
}
