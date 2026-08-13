package com.toy.cnr.application.game.service;

import com.toy.cnr.application.game.util.PolygonUtils;
import com.toy.cnr.domain.game.GameEvent;
import com.toy.cnr.domain.game.GemStatus;
import com.toy.cnr.domain.room.GeoPoint;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.GemStore;
import com.toy.cnr.port.game.model.GemDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 보석 스폰/조회 서비스.
 * <p>
 * 게임 시작 시 초기 보석을 스폰하고,
 * GemSpawnScheduler가 주기적으로 {@link #spawnIfNeeded}를 호출하여 보석 개수를 유지합니다.
 */
@Service
public class GemSpawnService {

    static final int MAX_GEMS = 5;
    static final int INITIAL_GEM_COUNT = 3;

    private static final Random RANDOM = new Random();

    private final GemStore gemStore;
    private final GameEventService gameEventService;

    public GemSpawnService(GemStore gemStore, GameEventService gameEventService) {
        this.gemStore = gemStore;
        this.gameEventService = gameEventService;
    }

    /**
     * 게임 시작 시 초기 보석을 스폰합니다.
     *
     * @param gameId   게임 ID
     * @param playArea 활동 구역 폴리곤
     */
    public void spawnInitialGems(String gameId, List<GeoPoint> playArea) {
        for (int i = 0; i < INITIAL_GEM_COUNT; i++) {
            spawnGem(gameId, playArea);
        }
    }

    /**
     * 현재 AVAILABLE 보석 수가 MAX_GEMS 미만이면 보석 1개를 스폰합니다.
     * GemSpawnScheduler에서 주기적으로 호출합니다.
     *
     * @param gameId   게임 ID
     * @param playArea 활동 구역 폴리곤
     */
    public void spawnIfNeeded(String gameId, List<GeoPoint> playArea) {
        var result = gemStore.getAllGems(gameId);
        if (!(result instanceof RepositoryResult.Found<List<GemDto>> found)) {
            return;
        }
        long availableCount = found.data().stream()
            .filter(g -> g.status().equals(GemStatus.AVAILABLE.name()))
            .count();

        if (availableCount < MAX_GEMS) {
            spawnGem(gameId, playArea);
        }
    }

    private void spawnGem(String gameId, List<GeoPoint> playArea) {
        if (playArea == null || playArea.isEmpty()) {
            return;
        }
        var point = PolygonUtils.randomPoint(playArea, RANDOM);
        if (point == null) {
            return;
        }
        var gemId = UUID.randomUUID().toString();
        var now = System.currentTimeMillis();
        var gem = new GemDto(gemId, point.latitude(), point.longitude(),
            GemStatus.AVAILABLE.name(), null, now);

        gemStore.saveGem(gameId, gem);

        gameEventService.publish(new GameEvent.GemSpawned(
            gameId, gemId, point.latitude(), point.longitude(), now
        ));
    }
}
