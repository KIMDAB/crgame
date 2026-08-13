package com.toy.cnr.cache.game;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toy.cnr.cache.game.key.GameKey;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.room.RoomStore;
import com.toy.cnr.port.room.model.RoomDto;
import com.toy.cnr.port.room.model.RoomPlayerDto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Redis HASH 기반 방(Room) 저장소 구현.
 * <p>
 * room:{roomId}          — 방 메타데이터 (HASH field: "data" → JSON)<br>
 * room:{roomId}:players  — 참가자 (HASH field: playerId → JSON)
 */
@Repository
public class RoomRedisStore implements RoomStore {

    private static final String DATA_FIELD = "data";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RoomRedisStore(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public RepositoryResult<Void> saveRoom(RoomDto room) {
        try {
            var key = GameKey.room(room.roomId());
            redisTemplate.opsForHash().put(key, DATA_FIELD, objectMapper.writeValueAsString(room));
            return new RepositoryResult.Found<>(null);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<RoomDto> getRoom(String roomId) {
        try {
            var key = GameKey.room(roomId);
            var value = redisTemplate.opsForHash().get(key, DATA_FIELD);
            if (value == null) {
                return new RepositoryResult.NotFound<>("Room not found: " + roomId);
            }
            return new RepositoryResult.Found<>(objectMapper.readValue(value.toString(), RoomDto.class));
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<Void> addPlayer(String roomId, RoomPlayerDto player) {
        try {
            var key = GameKey.roomPlayers(roomId);
            redisTemplate.opsForHash().put(key, player.playerId(), objectMapper.writeValueAsString(player));
            return new RepositoryResult.Found<>(null);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<Void> removePlayer(String roomId, String playerId) {
        try {
            var key = GameKey.roomPlayers(roomId);
            redisTemplate.opsForHash().delete(key, playerId);
            return new RepositoryResult.Found<>(null);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<List<RoomPlayerDto>> getPlayers(String roomId) {
        try {
            var key = GameKey.roomPlayers(roomId);
            var values = redisTemplate.opsForHash().values(key);
            var players = values.stream()
                .map(v -> {
                    try {
                        return objectMapper.readValue(v.toString(), RoomPlayerDto.class);
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
    public RepositoryResult<Void> updateStatus(String roomId, String status) {
        try {
            var result = getRoom(roomId);
            if (result instanceof RepositoryResult.NotFound<RoomDto> notFound) {
                return new RepositoryResult.NotFound<>(notFound.message());
            }
            if (result instanceof RepositoryResult.Error<RoomDto> error) {
                return new RepositoryResult.Error<>(error.t());
            }
            var room = ((RepositoryResult.Found<RoomDto>) result).data();
            var updated = new RoomDto(
                room.roomId(),
                room.hostId(),
                room.settings(),
                status,
                room.players(),
                room.createdAt()
            );
            return saveRoom(updated);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<Void> deleteRoom(String roomId) {
        try {
            redisTemplate.delete(GameKey.room(roomId));
            redisTemplate.delete(GameKey.roomPlayers(roomId));
            return new RepositoryResult.Found<>(null);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }
}
