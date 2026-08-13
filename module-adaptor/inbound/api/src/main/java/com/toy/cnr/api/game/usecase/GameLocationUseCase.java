package com.toy.cnr.api.game.usecase;

import com.toy.cnr.api.game.request.LocationPublishRequest;
import com.toy.cnr.api.game.response.LocationResponse;
import com.toy.cnr.application.game.service.LocationService;
import com.toy.cnr.domain.common.CommandResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * 좌표 발행/구독 유즈케이스.
 * <p>
 * 플레이어 GPS 좌표를 Redis GeoHash에 저장하고 Pub/Sub으로 브로드캐스트합니다.
 * HTTP 관심사({@code SseEmitter}, {@code ResponseEntity})는 Controller가 담당합니다.
 */
@Component
public class GameLocationUseCase {

    private final LocationService locationService;

    public GameLocationUseCase(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * 현재 플레이어의 저장된 좌표를 조회합니다.
     *
     * @param gameId   게임 세션 ID
     * @param playerId 요청자(현재 사용자) 플레이어 ID
     * @return 조회 성공 시 LocationResponse, 없으면 BusinessError
     */
    public CommandResult<LocationResponse> getMyLocation(String gameId, String playerId) {
        return getPlayerLocation(gameId, playerId);
    }

    /**
     * 특정 플레이어의 저장된 좌표를 조회합니다.
     * 같은 게임에 참여한 플레이어의 최신 위치를 한 번 조회할 때 사용합니다.
     *
     * @param gameId   게임 세션 ID
     * @param playerId 조회할 플레이어 ID
     * @return 조회 성공 시 LocationResponse, 없으면 BusinessError (404)
     */
    public CommandResult<LocationResponse> getPlayerLocation(String gameId, String playerId) {
        return locationService.getLocation(gameId, playerId)
            .map(LocationResponse::from);
    }

    /**
     * 좌표를 발행합니다 (GeoHash 저장 + Pub/Sub 발행).
     */
    public CommandResult<LocationResponse> publishLocation(LocationPublishRequest request) {
        return locationService.publishLocation(request.toCommand())
            .map(LocationResponse::from);
    }

    /**
     * 여러 플레이어의 좌표를 구독합니다.
     * Consumer 콜백으로 좌표를 전달하며, HTTP 관심사(SseEmitter)를 알지 못합니다.
     *
     * @param gameId     게임 세션 ID
     * @param playerIds  구독할 플레이어 ID 목록
     * @param onLocation 좌표 수신 시 호출되는 콜백
     */
    public void subscribeToPlayers(
        String gameId,
        List<String> playerIds,
        Consumer<LocationResponse> onLocation
    ) {
        locationService.subscribe(gameId, playerIds, location ->
            onLocation.accept(LocationResponse.from(location))
        );
    }

    /**
     * 구독을 해제합니다.
     */
    public void unsubscribe(String gameId, List<String> playerIds) {
        locationService.unsubscribe(gameId, playerIds);
    }
}
