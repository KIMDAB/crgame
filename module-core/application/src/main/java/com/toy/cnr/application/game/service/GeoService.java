package com.toy.cnr.application.game.service;

import com.toy.cnr.application.common.ResultMapper;
import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.game.PlayerRole;
import com.toy.cnr.domain.game.PlayerStatus;
import com.toy.cnr.domain.game.RobberNearby;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.InGamePlayerStore;
import com.toy.cnr.port.game.LocationStore;
import com.toy.cnr.port.game.model.InGamePlayerDto;
import com.toy.cnr.port.game.model.LocationDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 위치 기반 조회 서비스 (거리, 반경 내 플레이어 등).
 * <p>
 * 경찰 반경 내 도둑 조회 등 Geo 관련 비즈니스 로직을 담당합니다.
 */
@Service
public class GeoService {

    // 500m 반경 내 도둑 조회
    private static final double MAX_RADIUS_METERS = 500.0;

    private final InGamePlayerStore inGamePlayerStore;
    private final LocationStore locationStore;

    public GeoService(InGamePlayerStore inGamePlayerStore, LocationStore locationStore) {
        this.inGamePlayerStore = inGamePlayerStore;
        this.locationStore = locationStore;
    }

    /**
     * 경찰 기준 반경(미터) 내에 있는 ACTIVE 상태 도둑 목록을 조회합니다.
     * 거리는 Redis GEODIST로 계산하며, 반경 값은 호출자가 임의로 지정합니다.
     *
     * @param gameId        게임 세션 ID
     * @param copsPlayerId  경찰(요청자) 플레이어 ID
     * @param radiusMeters  반경 (미터, 0 초과, 최대 {@value #MAX_RADIUS_METERS}m)
     * @return 반경 내 도둑 목록 (playerId, distanceMeters, longitude, latitude). 요청자가 경찰이 아니거나 반경이 유효하지 않으면 BusinessError
     */
    public CommandResult<List<RobberNearby>> getRobbersWithinRadius(
        String gameId,
        String copsPlayerId,
        double radiusMeters
    ) {
        if (radiusMeters <= 0 || radiusMeters > MAX_RADIUS_METERS) {
            return new CommandResult.BusinessError<>(
                "radiusMeters must be > 0 and <= " + MAX_RADIUS_METERS
            );
        }

        var copsResult = inGamePlayerStore.getPlayer(gameId, copsPlayerId);
        return ResultMapper.toCommandResult(copsResult).flatMap(cops -> {
            if (!cops.role().equals(PlayerRole.POLICE.name())) {
                return new CommandResult.BusinessError<>("Only cops can query robbers nearby");
            }

            var allResult = inGamePlayerStore.getAllPlayers(gameId);
            if (!(allResult instanceof RepositoryResult.Found<List<InGamePlayerDto>> found)) {
                return new CommandResult.BusinessError<>("Failed to load players");
            }

            List<RobberNearby> nearby = new ArrayList<>();
            for (var player : found.data()) {
                if (!player.role().equals(PlayerRole.THIEF.name())) {
                    continue;
                }
                if (!player.status().equals(PlayerStatus.ACTIVE.name())) {
                    continue;
                }
                String robberId = player.playerId();

                var distResult = locationStore.getDistanceMeters(gameId, copsPlayerId, robberId);
                if (!(distResult instanceof RepositoryResult.Found<Double> distFound)) {
                    continue;
                }
                double distance = distFound.data();
                if (distance > radiusMeters) {
                    continue;
                }

                var locResult = locationStore.getLocation(gameId, robberId);
                if (!(locResult instanceof RepositoryResult.Found<LocationDto> locFound)) {
                    continue;
                }
                var loc = locFound.data();
                nearby.add(new RobberNearby(
                    robberId,
                    distance,
                    loc.longitude(),
                    loc.latitude()
                ));
            }

            return new CommandResult.Success<>(nearby, null);
        });
    }
}
