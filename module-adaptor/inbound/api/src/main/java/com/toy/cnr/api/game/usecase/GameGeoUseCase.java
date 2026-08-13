package com.toy.cnr.api.game.usecase;

import com.toy.cnr.api.game.response.RobberNearbyResponse;
import com.toy.cnr.application.game.service.GeoService;
import com.toy.cnr.domain.common.CommandResult;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Geo(거리/반경) 조회 유즈케이스.
 * <p>
 * 경찰 반경 내 도둑 조회 등 위치 기반 조회 API를 오케스트레이션합니다.
 */
@Component
public class GameGeoUseCase {

    private final GeoService geoService;

    public GameGeoUseCase(GeoService geoService) {
        this.geoService = geoService;
    }

    /**
     * 경찰 기준 반경(미터) 내에 있는 도둑 목록을 조회합니다.
     * 반경 값은 호출자가 임의로 지정합니다 (0 초과, 최대 10000m).
     *
     * @param gameId        게임 세션 ID
     * @param copsPlayerId  경찰(현재 사용자) 플레이어 ID
     * @param radiusMeters  반경 (미터)
     * @return 반경 내 도둑 목록 (playerId, distanceMeters, longitude, latitude)
     */
    public CommandResult<List<RobberNearbyResponse>> getRobbersNearby(
        String gameId,
        String copsPlayerId,
        double radiusMeters
    ) {
        return geoService.getRobbersWithinRadius(gameId, copsPlayerId, radiusMeters)
            .map(list -> list.stream().map(RobberNearbyResponse::from).toList());
    }
}
