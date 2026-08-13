package com.toy.cnr.port.game;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.model.LocationDto;

/**
 * 좌표 저장/조회 포트 인터페이스.
 * <p>
 * Redis GeoHash를 통해 플레이어 좌표를 저장하고 조회합니다.
 */
public interface LocationStore {

    /**
     * 플레이어 좌표를 저장합니다 (GeoHash).
     */
    RepositoryResult<Void> saveLocation(
        String gameId,
        String playerId,
        double longitude,
        double latitude
    );

    /**
     * 플레이어 좌표를 조회합니다.
     */
    RepositoryResult<LocationDto> getLocation(String gameId, String playerId);

    /**
     * 두 플레이어 사이의 거리를 미터 단위로 계산합니다 (Redis GEODIST).
     */
    RepositoryResult<Double> getDistanceMeters(String gameId, String playerId1, String playerId2);
}
