package com.toy.cnr.batch.game;

import com.toy.cnr.application.game.service.GemSpawnService;
import com.toy.cnr.domain.game.GameStatus;
import com.toy.cnr.domain.room.GeoPoint;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.GameRegistryStore;
import com.toy.cnr.port.game.GameStateStore;
import com.toy.cnr.port.game.model.GameStateDto;
import com.toy.cnr.port.room.model.RoomSettingsDto;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 보석 자동 스폰 스케줄러.
 * <p>
 * 30초마다 활성 게임 목록을 순회하며, PLAYING 상태인 게임의 AVAILABLE 보석 수가
 * {@link GemSpawnService#MAX_GEMS} 미만이면 보석 1개를 스폰합니다.
 */
@Component
public class GemSpawnScheduler {

    private final GameRegistryStore gameRegistryStore;
    private final GameStateStore gameStateStore;
    private final GemSpawnService gemSpawnService;

    public GemSpawnScheduler(
        GameRegistryStore gameRegistryStore,
        GameStateStore gameStateStore,
        GemSpawnService gemSpawnService
    ) {
        this.gameRegistryStore = gameRegistryStore;
        this.gameStateStore = gameStateStore;
        this.gemSpawnService = gemSpawnService;
    }

    @Scheduled(fixedDelay = 30_000)
    public void run() {
        var registryResult = gameRegistryStore.getActiveGameIds();
        if (!(registryResult instanceof RepositoryResult.Found<Set<String>> found)) {
            return;
        }

        for (var gameId : found.data()) {
            var stateResult = gameStateStore.getGameState(gameId);
            if (!(stateResult instanceof RepositoryResult.Found<GameStateDto> stateFound)) {
                continue;
            }
            var state = stateFound.data();
            if (!state.status().equals(GameStatus.PLAYING.name())) {
                continue;
            }
            var playArea = extractPlayArea(state.settings());
            gemSpawnService.spawnIfNeeded(gameId, playArea);
        }
    }

    private List<GeoPoint> extractPlayArea(RoomSettingsDto settings) {
        if (settings == null || settings.playArea() == null) {
            return Collections.emptyList();
        }
        return settings.playArea().stream()
            .map(p -> new GeoPoint(p.latitude(), p.longitude()))
            .toList();
    }
}
