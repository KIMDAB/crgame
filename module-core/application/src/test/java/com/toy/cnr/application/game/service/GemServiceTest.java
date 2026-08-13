package com.toy.cnr.application.game.service;

import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.game.Gem;
import com.toy.cnr.domain.game.GemStatus;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.GemStore;
import com.toy.cnr.port.game.model.GemDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GemServiceTest {

    private GemStore gemStore;
    private GemService gemService;

    @BeforeEach
    void setUp() {
        gemStore = Mockito.mock(GemStore.class);
        gemService = new GemService(gemStore);
    }

    private static final String GAME_ID = "game-001";

    private static GemDto availableGem(String id) {
        return new GemDto(id, 37.0, 127.0, GemStatus.AVAILABLE.name(), null, 1_000L);
    }

    private static GemDto collectedGem(String id) {
        return new GemDto(id, 37.5, 127.5, GemStatus.COLLECTED.name(), "robber-1", 1_000L);
    }

    // ────────────────────────────────────────────────────
    // getAvailableGems
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAvailableGems")
    class GetAvailableGems {

        @Test
        @DisplayName("[성공] AVAILABLE 보석만 필터링되어 반환")
        void getAvailableGems_filtersCollected() {
            when(gemStore.getAllGems(GAME_ID)).thenReturn(new RepositoryResult.Found<>(
                List.of(availableGem("gem-1"), availableGem("gem-2"), collectedGem("gem-3"))
            ));

            var result = gemService.getAvailableGems(GAME_ID);

            assertInstanceOf(CommandResult.Success.class, result);
            var gems = ((CommandResult.Success<List<Gem>>) result).data();
            assertEquals(2, gems.size());
            assertTrue(gems.stream().allMatch(g -> g.status() == GemStatus.AVAILABLE));
        }

        @Test
        @DisplayName("[성공] 모든 보석이 COLLECTED인 경우 빈 리스트 반환")
        void getAvailableGems_allCollected_returnsEmpty() {
            when(gemStore.getAllGems(GAME_ID)).thenReturn(new RepositoryResult.Found<>(
                List.of(collectedGem("gem-1"), collectedGem("gem-2"))
            ));

            var result = gemService.getAvailableGems(GAME_ID);

            assertInstanceOf(CommandResult.Success.class, result);
            assertTrue(((CommandResult.Success<List<Gem>>) result).data().isEmpty());
        }

        @Test
        @DisplayName("[성공] 보석이 없는 경우 빈 리스트 반환")
        void getAvailableGems_noGems_returnsEmpty() {
            when(gemStore.getAllGems(GAME_ID)).thenReturn(new RepositoryResult.Found<>(List.of()));

            var result = gemService.getAvailableGems(GAME_ID);

            assertInstanceOf(CommandResult.Success.class, result);
            assertTrue(((CommandResult.Success<List<Gem>>) result).data().isEmpty());
        }

        @Test
        @DisplayName("[실패] 저장소 오류 (Error) → BusinessError")
        void getAvailableGems_storeError() {
            when(gemStore.getAllGems(GAME_ID))
                .thenReturn(new RepositoryResult.Error<>(new RuntimeException("Redis error")));

            var result = gemService.getAvailableGems(GAME_ID);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }

        @Test
        @DisplayName("[실패] 게임 없음 (NotFound) → BusinessError")
        void getAvailableGems_notFound() {
            when(gemStore.getAllGems(GAME_ID))
                .thenReturn(new RepositoryResult.NotFound<>("Game not found"));

            var result = gemService.getAvailableGems(GAME_ID);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }
    }

    // ────────────────────────────────────────────────────
    // getAllGems
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllGems")
    class GetAllGems {

        @Test
        @DisplayName("[성공] AVAILABLE/COLLECTED 보석 모두 반환, 도메인 모델 매핑 검증")
        void getAllGems_returnsAll() {
            when(gemStore.getAllGems(GAME_ID)).thenReturn(new RepositoryResult.Found<>(
                List.of(availableGem("gem-1"), collectedGem("gem-2"))
            ));

            var result = gemService.getAllGems(GAME_ID);

            assertInstanceOf(CommandResult.Success.class, result);
            var gems = ((CommandResult.Success<List<Gem>>) result).data();
            assertEquals(2, gems.size());

            var available = gems.stream().filter(g -> g.gemId().equals("gem-1")).findFirst().orElseThrow();
            assertEquals(GemStatus.AVAILABLE, available.status());
            assertNull(available.collectedBy());

            var collected = gems.stream().filter(g -> g.gemId().equals("gem-2")).findFirst().orElseThrow();
            assertEquals(GemStatus.COLLECTED, collected.status());
            assertEquals("robber-1", collected.collectedBy());
        }

        @Test
        @DisplayName("[성공] 보석이 없는 경우 빈 리스트 반환")
        void getAllGems_empty() {
            when(gemStore.getAllGems(GAME_ID)).thenReturn(new RepositoryResult.Found<>(List.of()));

            var result = gemService.getAllGems(GAME_ID);

            assertInstanceOf(CommandResult.Success.class, result);
            assertTrue(((CommandResult.Success<List<Gem>>) result).data().isEmpty());
        }

        @Test
        @DisplayName("[실패] 저장소 오류 (Error) → BusinessError")
        void getAllGems_storeError() {
            when(gemStore.getAllGems(GAME_ID))
                .thenReturn(new RepositoryResult.Error<>(new RuntimeException("Redis error")));

            var result = gemService.getAllGems(GAME_ID);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }

        @Test
        @DisplayName("[실패] 게임 없음 (NotFound) → BusinessError")
        void getAllGems_notFound() {
            when(gemStore.getAllGems(GAME_ID))
                .thenReturn(new RepositoryResult.NotFound<>("Game not found"));

            var result = gemService.getAllGems(GAME_ID);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }
    }
}
