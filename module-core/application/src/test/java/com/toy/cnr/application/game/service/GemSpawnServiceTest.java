package com.toy.cnr.application.game.service;

import com.toy.cnr.domain.game.GameEvent;
import com.toy.cnr.domain.game.GemStatus;
import com.toy.cnr.domain.room.GeoPoint;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.GemStore;
import com.toy.cnr.port.game.model.GemDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GemSpawnServiceTest {

    private GemStore gemStore;
    private GameEventService gameEventService;
    private GemSpawnService gemSpawnService;

    @BeforeEach
    void setUp() {
        gemStore = Mockito.mock(GemStore.class);
        gameEventService = Mockito.mock(GameEventService.class);
        gemSpawnService = new GemSpawnService(gemStore, gameEventService);
        when(gemStore.saveGem(anyString(), any())).thenReturn(new RepositoryResult.Found<>(null));
    }

    private static final String GAME_ID = "game-001";

    /**
     * 10×10 degree 정사각형 폴리곤.
     * 바운딩 박스와 동일한 볼록 폴리곤이므로 randomPoint가 항상 유효한 점을 반환합니다.
     */
    private static List<GeoPoint> squarePlayArea() {
        return List.of(
            new GeoPoint(37.0, 127.0),
            new GeoPoint(37.0, 128.0),
            new GeoPoint(38.0, 128.0),
            new GeoPoint(38.0, 127.0)
        );
    }

    private static GemDto availableGem(String id) {
        return new GemDto(id, 37.5, 127.5, GemStatus.AVAILABLE.name(), null, 1_000L);
    }

    private static GemDto collectedGem(String id) {
        return new GemDto(id, 37.5, 127.5, GemStatus.COLLECTED.name(), "robber-1", 1_000L);
    }

    // ────────────────────────────────────────────────────
    // spawnInitialGems
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("spawnInitialGems")
    class SpawnInitialGems {

        @Test
        @DisplayName("[성공] INITIAL_GEM_COUNT(3)번 saveGem 호출 + GemSpawned 이벤트 발행")
        void spawnInitialGems_spawnsThreeGems() {
            gemSpawnService.spawnInitialGems(GAME_ID, squarePlayArea());

            verify(gemStore, times(GemSpawnService.INITIAL_GEM_COUNT)).saveGem(eq(GAME_ID), any());
            verify(gameEventService, times(GemSpawnService.INITIAL_GEM_COUNT)).publish(any(GameEvent.GemSpawned.class));
        }

        @Test
        @DisplayName("[성공] 저장된 보석은 AVAILABLE 상태이며 gameId와 좌표 포함")
        void spawnInitialGems_savedGemIsAvailable() {
            gemSpawnService.spawnInitialGems(GAME_ID, squarePlayArea());

            var captor = ArgumentCaptor.forClass(GemDto.class);
            verify(gemStore, times(GemSpawnService.INITIAL_GEM_COUNT)).saveGem(eq(GAME_ID), captor.capture());

            captor.getAllValues().forEach(gem -> {
                assertEquals(GemStatus.AVAILABLE.name(), gem.status());
                assertNull(gem.collectedBy());
                assertNotNull(gem.gemId());
                // 좌표가 playArea 범위(37~38, 127~128) 내에 있는지 검증
                assertTrue(gem.latitude() >= 37.0 && gem.latitude() <= 38.0);
                assertTrue(gem.longitude() >= 127.0 && gem.longitude() <= 128.0);
            });
        }

        @Test
        @DisplayName("[성공] GemSpawned 이벤트에 gemId, gameId, 좌표 포함")
        void spawnInitialGems_eventContainsGemInfo() {
            gemSpawnService.spawnInitialGems(GAME_ID, squarePlayArea());

            var captor = ArgumentCaptor.forClass(GameEvent.GemSpawned.class);
            verify(gameEventService, times(GemSpawnService.INITIAL_GEM_COUNT)).publish(captor.capture());

            captor.getAllValues().forEach(event -> {
                assertEquals(GAME_ID, event.gameId());
                assertNotNull(event.gemId());
                assertFalse(event.gemId().isBlank());
            });
        }

        @Test
        @DisplayName("[성공] playArea가 null → 스폰 없음, 이벤트 미발행")
        void spawnInitialGems_nullPlayArea_noSpawn() {
            gemSpawnService.spawnInitialGems(GAME_ID, null);

            verifyNoInteractions(gemStore, gameEventService);
        }

        @Test
        @DisplayName("[성공] playArea가 빈 리스트 → 스폰 없음, 이벤트 미발행")
        void spawnInitialGems_emptyPlayArea_noSpawn() {
            gemSpawnService.spawnInitialGems(GAME_ID, List.of());

            verifyNoInteractions(gemStore, gameEventService);
        }

        @Test
        @DisplayName("[성공] playArea가 꼭짓점 2개 이하(유효하지 않은 폴리곤) → 스폰 없음")
        void spawnInitialGems_invalidPolygon_noSpawn() {
            var twoPoints = List.of(new GeoPoint(37.0, 127.0), new GeoPoint(38.0, 128.0));

            gemSpawnService.spawnInitialGems(GAME_ID, twoPoints);

            verifyNoInteractions(gemStore, gameEventService);
        }
    }

    // ────────────────────────────────────────────────────
    // spawnIfNeeded
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("spawnIfNeeded")
    class SpawnIfNeeded {

        @Test
        @DisplayName("[성공] AVAILABLE 보석 수 < MAX_GEMS(5) → 보석 1개 스폰")
        void spawnIfNeeded_belowMax_spawnsOne() {
            // AVAILABLE 4개 → 1개 추가 필요
            when(gemStore.getAllGems(GAME_ID)).thenReturn(new RepositoryResult.Found<>(
                List.of(
                    availableGem("gem-1"), availableGem("gem-2"),
                    availableGem("gem-3"), availableGem("gem-4")
                )
            ));

            gemSpawnService.spawnIfNeeded(GAME_ID, squarePlayArea());

            verify(gemStore, times(1)).saveGem(eq(GAME_ID), any());
            verify(gameEventService, times(1)).publish(any(GameEvent.GemSpawned.class));
        }

        @Test
        @DisplayName("[성공] AVAILABLE 보석 수 = MAX_GEMS(5) → 스폰 없음")
        void spawnIfNeeded_atMax_noSpawn() {
            when(gemStore.getAllGems(GAME_ID)).thenReturn(new RepositoryResult.Found<>(
                List.of(
                    availableGem("gem-1"), availableGem("gem-2"), availableGem("gem-3"),
                    availableGem("gem-4"), availableGem("gem-5")
                )
            ));

            gemSpawnService.spawnIfNeeded(GAME_ID, squarePlayArea());

            verify(gemStore, never()).saveGem(anyString(), any());
            verifyNoInteractions(gameEventService);
        }

        @Test
        @DisplayName("[성공] COLLECTED 보석은 카운트에서 제외 → AVAILABLE만 기준으로 판단")
        void spawnIfNeeded_collectedNotCounted() {
            // AVAILABLE 1개 + COLLECTED 4개 → AVAILABLE < MAX_GEMS → 스폰 1회
            when(gemStore.getAllGems(GAME_ID)).thenReturn(new RepositoryResult.Found<>(
                List.of(
                    availableGem("gem-1"),
                    collectedGem("gem-2"), collectedGem("gem-3"),
                    collectedGem("gem-4"), collectedGem("gem-5")
                )
            ));

            gemSpawnService.spawnIfNeeded(GAME_ID, squarePlayArea());

            verify(gemStore, times(1)).saveGem(eq(GAME_ID), any());
        }

        @Test
        @DisplayName("[성공] getAllGems가 Found 아님 (Error/NotFound) → 스폰 없음")
        void spawnIfNeeded_storeError_noSpawn() {
            when(gemStore.getAllGems(GAME_ID))
                .thenReturn(new RepositoryResult.Error<>(new RuntimeException("Redis error")));

            gemSpawnService.spawnIfNeeded(GAME_ID, squarePlayArea());

            verify(gemStore, never()).saveGem(anyString(), any());
            verifyNoInteractions(gameEventService);
        }

        @Test
        @DisplayName("[성공] getAllGems가 NotFound → 스폰 없음")
        void spawnIfNeeded_notFound_noSpawn() {
            when(gemStore.getAllGems(GAME_ID))
                .thenReturn(new RepositoryResult.NotFound<>("Game not found"));

            gemSpawnService.spawnIfNeeded(GAME_ID, squarePlayArea());

            verify(gemStore, never()).saveGem(anyString(), any());
            verifyNoInteractions(gameEventService);
        }
    }
}
