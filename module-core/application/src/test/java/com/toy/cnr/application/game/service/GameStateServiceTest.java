package com.toy.cnr.application.game.service;

import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.game.*;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.GameStateStore;
import com.toy.cnr.port.game.InGamePlayerStore;
import com.toy.cnr.port.game.model.GameStateDto;
import com.toy.cnr.port.game.model.InGamePlayerDto;
import com.toy.cnr.port.room.model.RoomSettingsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class GameStateServiceTest {

    private GameStateStore gameStateStore;
    private InGamePlayerStore inGamePlayerStore;
    private GameStateService gameStateService;

    @BeforeEach
    void setUp() {
        gameStateStore = Mockito.mock(GameStateStore.class);
        inGamePlayerStore = Mockito.mock(InGamePlayerStore.class);
        gameStateService = new GameStateService(gameStateStore, inGamePlayerStore);
    }

    // ────────────────────────────────────────────────────
    // Fixtures
    // ────────────────────────────────────────────────────

    private static final String GAME_ID = "game-001";
    private static final String ROOM_ID = "ABCD";
    private static final String PLAYER_ID = "player-001";
    private static final String PLAYER_NAME = "PlayerOne";

    private static RoomSettingsDto settingsDto() {
        return new RoomSettingsDto(
            "BASIC", 2, 10, 1, 1, 10, 1, 5.0,
            null, null, null, null
        );
    }

    private static GameStateDto playingStateDto() {
        return new GameStateDto(
            GAME_ID, ROOM_ID, GameStatus.PLAYING.name(),
            settingsDto(), 1_000L, 601_000L
        );
    }

    private static InGamePlayerDto copsPlayerDto() {
        return new InGamePlayerDto(
            PLAYER_ID, PLAYER_NAME, PlayerRole.POLICE.name(), PlayerStatus.ACTIVE.name(),
            2, 0, 0, 0, System.currentTimeMillis()
        );
    }

    private static InGamePlayerDto robberPlayerDto() {
        return new InGamePlayerDto(
            "robber-001", "RobberOne", PlayerRole.THIEF.name(), PlayerStatus.ARRESTED.name(),
            0, 3, 1, 0, System.currentTimeMillis()
        );
    }

    // ────────────────────────────────────────────────────
    // getGameState
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("getGameState")
    class GetGameState {

        @Test
        @DisplayName("[성공] 정상 조회 → GameState 도메인 모델 반환 및 필드 검증")
        void getGameState_success() {
            when(gameStateStore.getGameState(GAME_ID))
                .thenReturn(new RepositoryResult.Found<>(playingStateDto()));

            var result = gameStateService.getGameState(GAME_ID);

            assertInstanceOf(CommandResult.Success.class, result);
            var state = ((CommandResult.Success<GameState>) result).data();
            assertEquals(GAME_ID, state.gameId());
            assertEquals(ROOM_ID, state.roomId());
            assertEquals(GameStatus.PLAYING, state.status());
            assertEquals(1_000L, state.startedAt());
        }

        @Test
        @DisplayName("[실패] 게임 없음 (NotFound) → BusinessError")
        void getGameState_notFound() {
            when(gameStateStore.getGameState(GAME_ID))
                .thenReturn(new RepositoryResult.NotFound<>("Game not found"));

            var result = gameStateService.getGameState(GAME_ID);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }

        @Test
        @DisplayName("[실패] 저장소 오류 (Error) → BusinessError")
        void getGameState_storeError() {
            when(gameStateStore.getGameState(GAME_ID))
                .thenReturn(new RepositoryResult.Error<>(new RuntimeException("Redis error")));

            var result = gameStateService.getGameState(GAME_ID);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }
    }

    // ────────────────────────────────────────────────────
    // getPlayers
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("getPlayers")
    class GetPlayers {

        @Test
        @DisplayName("[성공] 전체 플레이어 조회 → 역할/상태/통계 매핑 검증")
        void getPlayers_success() {
            when(inGamePlayerStore.getAllPlayers(GAME_ID))
                .thenReturn(new RepositoryResult.Found<>(
                    List.of(copsPlayerDto(), robberPlayerDto())
                ));

            var result = gameStateService.getPlayers(GAME_ID);

            assertInstanceOf(CommandResult.Success.class, result);
            var players = ((CommandResult.Success<List<InGamePlayer>>) result).data();
            assertEquals(2, players.size());

            var cops = players.stream().filter(p -> p.role() == PlayerRole.POLICE).findFirst().orElseThrow();
            assertEquals(PLAYER_ID, cops.playerId());
            assertEquals(PlayerStatus.ACTIVE, cops.status());
            assertEquals(2, cops.stats().arrestCount());

            var robber = players.stream().filter(p -> p.role() == PlayerRole.THIEF).findFirst().orElseThrow();
            assertEquals(PlayerStatus.ARRESTED, robber.status());
            assertEquals(3, robber.stats().gemsCollected());
        }

        @Test
        @DisplayName("[성공] 플레이어가 없는 경우 빈 리스트 반환")
        void getPlayers_emptyList() {
            when(inGamePlayerStore.getAllPlayers(GAME_ID))
                .thenReturn(new RepositoryResult.Found<>(List.of()));

            var result = gameStateService.getPlayers(GAME_ID);

            assertInstanceOf(CommandResult.Success.class, result);
            assertTrue(((CommandResult.Success<List<InGamePlayer>>) result).data().isEmpty());
        }

        @Test
        @DisplayName("[실패] 게임 없음 (NotFound) → BusinessError")
        void getPlayers_notFound() {
            when(inGamePlayerStore.getAllPlayers(GAME_ID))
                .thenReturn(new RepositoryResult.NotFound<>("Game not found"));

            var result = gameStateService.getPlayers(GAME_ID);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }

        @Test
        @DisplayName("[실패] 저장소 오류 (Error) → BusinessError")
        void getPlayers_storeError() {
            when(inGamePlayerStore.getAllPlayers(GAME_ID))
                .thenReturn(new RepositoryResult.Error<>(new RuntimeException("Redis error")));

            var result = gameStateService.getPlayers(GAME_ID);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }
    }

    // ────────────────────────────────────────────────────
    // getPlayer
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("getPlayer")
    class GetPlayer {

        @Test
        @DisplayName("[성공] 단일 플레이어 조회 → InGamePlayer 반환 및 필드 검증")
        void getPlayer_success() {
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Found<>(copsPlayerDto()));

            var result = gameStateService.getPlayer(GAME_ID, PLAYER_ID);

            assertInstanceOf(CommandResult.Success.class, result);
            var player = ((CommandResult.Success<InGamePlayer>) result).data();
            assertEquals(PLAYER_ID, player.playerId());
            assertEquals(PLAYER_NAME, player.playerName());
            assertEquals(PlayerRole.POLICE, player.role());
            assertEquals(PlayerStatus.ACTIVE, player.status());
        }

        @Test
        @DisplayName("[실패] 플레이어 없음 (NotFound) → BusinessError")
        void getPlayer_notFound() {
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.NotFound<>("Player not found"));

            var result = gameStateService.getPlayer(GAME_ID, PLAYER_ID);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }

        @Test
        @DisplayName("[실패] 저장소 오류 (Error) → BusinessError")
        void getPlayer_storeError() {
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Error<>(new RuntimeException("Redis error")));

            var result = gameStateService.getPlayer(GAME_ID, PLAYER_ID);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }
    }
}
