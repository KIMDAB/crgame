package com.toy.cnr.application.game.service;

import com.toy.cnr.application.game.mapper.LocationMapper;
import com.toy.cnr.application.game.util.PolygonUtils;
import com.toy.cnr.application.common.ResultMapper;
import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.game.GameEvent;
import com.toy.cnr.domain.game.LocationPublishCommand;
import com.toy.cnr.domain.game.PlayerLocation;
import com.toy.cnr.domain.game.PlayerStatus;
import com.toy.cnr.domain.room.GeoPoint;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.*;
import com.toy.cnr.port.game.model.GameStateDto;
import com.toy.cnr.port.game.model.InGamePlayerDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

/**
 * 좌표 발행/구독 서비스.
 * <p>
 * LocationStore(GeoHash), LocationPublisher(Pub/Sub), LocationSubscriber 포트를 사용합니다.
 * <p>
 * 체포된 플레이어가 위치를 업데이트할 때 감옥 폴리곤 이탈 여부를 실시간으로 감지하여
 * {@link GameEvent.PrisonEscapeWarning} 이벤트를 발행합니다.
 */
@Service
public class LocationService {

    private final LocationStore locationStore;
    private final LocationPublisher locationPublisher;
    private final LocationSubscriber locationSubscriber;
    private final InGamePlayerStore inGamePlayerStore;
    private final GameStateStore gameStateStore;
    private final GameEventService gameEventService;

    public LocationService(
        LocationStore locationStore,
        LocationPublisher locationPublisher,
        LocationSubscriber locationSubscriber,
        InGamePlayerStore inGamePlayerStore,
        GameStateStore gameStateStore,
        GameEventService gameEventService
    ) {
        this.locationStore = locationStore;
        this.locationPublisher = locationPublisher;
        this.locationSubscriber = locationSubscriber;
        this.inGamePlayerStore = inGamePlayerStore;
        this.gameStateStore = gameStateStore;
        this.gameEventService = gameEventService;
    }

    /**
     * 플레이어 좌표를 GeoHash에 저장하고 Pub/Sub으로 발행합니다.
     * 체포 상태의 플레이어는 감옥 폴리곤 이탈 여부를 추가로 검사합니다.
     */
    public CommandResult<PlayerLocation> publishLocation(LocationPublishCommand command) {
        long timestamp = System.currentTimeMillis();

        var previousLocation = locationStore.getLocation(command.gameId(), command.playerId());
        var storeResult = locationStore.saveLocation(
            command.gameId(),
            command.playerId(),
            command.longitude(),
            command.latitude()
        );

        return ResultMapper.toCommandResult(storeResult)
            .map(unused -> {
                var dto = LocationMapper.toDto(command, timestamp);
                locationPublisher.publish(command.gameId(), command.playerId(), dto);
                checkPrisonEscape(command.gameId(), command.playerId(), command.latitude(), command.longitude());
                checkRestrictedAreaEntered(
                    command.gameId(),
                    command.playerId(),
                    previousLocation,
                    command.latitude(),
                    command.longitude()
                );
                return LocationMapper.fromCommand(command, timestamp);
            });
    }

    /**
     * 저장된 플레이어 좌표를 조회합니다.
     *
     * @param gameId   게임 세션 ID
     * @param playerId 플레이어 ID
     * @return 조회 성공 시 PlayerLocation, 없거나 실패 시 CommandResult.BusinessError
     */
    public CommandResult<PlayerLocation> getLocation(String gameId, String playerId) {
        var result = locationStore.getLocation(gameId, playerId);
        return ResultMapper.toCommandResult(result)
            .map(dto -> LocationMapper.toDomain(gameId, dto));
    }

    /**
     * 여러 플레이어의 좌표 채널을 구독합니다.
     */
    public void subscribe(
        String gameId,
        List<String> playerIds,
        Consumer<PlayerLocation> onLocation
    ) {
        for (var playerId : playerIds) {
            locationSubscriber.subscribe(gameId, playerId, dto ->
                onLocation.accept(LocationMapper.toDomain(gameId, dto))
            );
        }
    }

    /**
     * 여러 플레이어의 좌표 채널 구독을 해제합니다.
     */
    public void unsubscribe(String gameId, List<String> playerIds) {
        for (var playerId : playerIds) {
            locationSubscriber.unsubscribe(gameId, playerId);
        }
    }

    /**
     * 체포된 플레이어의 위치가 감옥 폴리곤을 벗어났는지 검사합니다.
     * 이탈 감지 시 PrisonEscapeWarning 이벤트를 SSE 스트림으로 발행합니다.
     */
    private void checkPrisonEscape(String gameId, String playerId, double lat, double lon) {
        var playerResult = inGamePlayerStore.getPlayer(gameId, playerId);
        if (!(playerResult instanceof RepositoryResult.Found<InGamePlayerDto> playerFound)) {
            return;
        }
        if (!playerFound.data().status().equals(PlayerStatus.ARRESTED.name())) {
            return;
        }

        var stateResult = gameStateStore.getGameState(gameId);
        if (!(stateResult instanceof RepositoryResult.Found<GameStateDto> stateFound)) {
            return;
        }

        var settings = stateFound.data().settings();
        if (settings == null || settings.prisonArea() == null || settings.prisonArea().isEmpty()) {
            return;
        }

        List<GeoPoint> prisonArea = settings.prisonArea().stream()
            .map(p -> new GeoPoint(p.latitude(), p.longitude()))
            .toList();

        if (!PolygonUtils.contains(prisonArea, lat, lon)) {
            gameEventService.publish(new GameEvent.PrisonEscapeWarning(
                gameId, playerId, System.currentTimeMillis()
            ));
        }
    }

    /**
     * 플레이어가 restrictedArea(위험/제한 구역)에 "진입"했는지 검사합니다.
     * <p>
     * 이전 위치는 saveLocation 이전에 읽은 값으로 판단합니다.
     */
    private void checkRestrictedAreaEntered(
        String gameId,
        String playerId,
        RepositoryResult<com.toy.cnr.port.game.model.LocationDto> previousLocation,
        double lat,
        double lon
    ) {
        var stateResult = gameStateStore.getGameState(gameId);
        if (!(stateResult instanceof RepositoryResult.Found<GameStateDto> stateFound)) {
            return;
        }

        var settings = stateFound.data().settings();
        if (settings == null || settings.restrictedArea() == null || settings.restrictedArea().isEmpty()) {
            return;
        }

        List<GeoPoint> restrictedArea = settings.restrictedArea().stream()
            .map(p -> new GeoPoint(p.latitude(), p.longitude()))
            .toList();

        boolean nowInside = PolygonUtils.contains(restrictedArea, lat, lon);
        if (!nowInside) {
            return;
        }

        boolean prevInside = false;
        if (previousLocation instanceof RepositoryResult.Found<com.toy.cnr.port.game.model.LocationDto> found) {
            var prev = found.data();
            prevInside = PolygonUtils.contains(restrictedArea, prev.latitude(), prev.longitude());
        }

        if (!prevInside) {
            gameEventService.publish(new GameEvent.RestrictedAreaEntered(
                gameId, playerId, lat, lon, System.currentTimeMillis()
            ));
        }
    }
}
