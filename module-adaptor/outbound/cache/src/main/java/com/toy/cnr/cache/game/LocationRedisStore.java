package com.toy.cnr.cache.game;

import com.toy.cnr.cache.game.key.GameKey;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.LocationStore;
import com.toy.cnr.port.game.model.LocationDto;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Redis GeoHash 기반 좌표 저장소 구현.
 * <p>
 * Redis key: {@code game:{gameId}:locations}
 * <br>
 * Redis command: GEOADD, GEOPOS
 */
@Repository
public class LocationRedisStore implements LocationStore {

    private final RedisTemplate<String, Object> redisTemplate;

    public LocationRedisStore(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RepositoryResult<Void> saveLocation(
        String gameId,
        String playerId,
        double longitude,
        double latitude
    ) {
        try {
            var key = GameKey.locations(gameId);
            redisTemplate.opsForGeo().add(key, new Point(longitude, latitude), playerId);
            return new RepositoryResult.Found<>(null);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<LocationDto> getLocation(String gameId, String playerId) {
        try {
            var key = GameKey.locations(gameId);
            var positions = redisTemplate.opsForGeo().position(key, playerId);

            if (positions == null || positions.isEmpty() || positions.getFirst() == null) {
                return new RepositoryResult.NotFound<>(
                    "Location not found for player: " + playerId
                );
            }

            var point = positions.getFirst();
            return new RepositoryResult.Found<>(new LocationDto(
                playerId,
                point.getX(),
                point.getY(),
                System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<Double> getDistanceMeters(String gameId, String playerId1, String playerId2) {
        try {
            var key = GameKey.locations(gameId);
            Distance distance = redisTemplate.opsForGeo().distance(key, playerId1, playerId2, Metrics.METERS);
            if (distance == null) {
                return new RepositoryResult.NotFound<>(
                    "Location not found for one of the players: " + playerId1 + ", " + playerId2
                );
            }
            return new RepositoryResult.Found<>(distance.getValue());
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

}
