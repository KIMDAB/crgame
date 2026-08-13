package com.toy.cnr.application.game.service;

import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.game.*;
import com.toy.cnr.domain.room.RoomPlayer;
import com.toy.cnr.domain.room.RoomSettings;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.*;
import com.toy.cnr.port.game.model.GameStateDto;
import com.toy.cnr.port.game.model.GemDto;
import com.toy.cnr.port.game.model.InGamePlayerDto;
import com.toy.cnr.port.game.model.LocationDto;
import com.toy.cnr.port.room.model.RoomSettingsDto;
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

class GameActionServiceTest {

    private GameStateStore gameStateStore;
    private InGamePlayerStore inGamePlayerStore;
    private GemStore gemStore;
    private LocationStore locationStore;
    private GameEventService gameEventService;
    private GameRegistryStore gameRegistryStore;
    private GameTimerService gameTimerService;
    private GemSpawnService gemSpawnService;
    private GameActionService gameActionService;

    @BeforeEach
    void setUp() {
        gameStateStore = Mockito.mock(GameStateStore.class);
        inGamePlayerStore = Mockito.mock(InGamePlayerStore.class);
        gemStore = Mockito.mock(GemStore.class);
        locationStore = Mockito.mock(LocationStore.class);
        gameEventService = Mockito.mock(GameEventService.class);
        gameRegistryStore = Mockito.mock(GameRegistryStore.class);
        gameTimerService = Mockito.mock(GameTimerService.class);
        gemSpawnService = Mockito.mock(GemSpawnService.class);
        gameActionService = new GameActionService(
            gameStateStore, inGamePlayerStore, gemStore, locationStore,
            gameEventService, gameRegistryStore, gameTimerService, gemSpawnService
        );
    }

    // ────────────────────────────────────────────────────
    // Fixtures
    // ────────────────────────────────────────────────────

    private static final String GAME_ID = "game-001";
    private static final String ROOM_ID = "ABCD";
    private static final String COPS_ID = "cops-001";
    private static final String COPS_NAME = "CopsPlayer";
    private static final String ROBBER_ID = "robber-001";
    private static final String ROBBER_NAME = "RobberPlayer";
    private static final String GEM_ID = "gem-001";
    private static final double ACTION_RADIUS = 5.0;
    private static final double WITHIN_RANGE = 2.0;
    private static final double OUT_OF_RANGE = 100.0;

    private static RoomSettingsDto defaultSettingsDto() {
        return new RoomSettingsDto(
            "BASIC", 2, 10, 1, 1, 10, 1, ACTION_RADIUS,
            null, null, null, null
        );
    }

    private static GameStateDto gameStateDto() {
        return new GameStateDto(
            GAME_ID, ROOM_ID, GameStatus.PLAYING.name(),
            defaultSettingsDto(), System.currentTimeMillis(), System.currentTimeMillis() + 600_000
        );
    }

    private static InGamePlayerDto copsDto() {
        return new InGamePlayerDto(
            COPS_ID, COPS_NAME, PlayerRole.POLICE.name(), PlayerStatus.ACTIVE.name(),
            0, 0, 0, 0, System.currentTimeMillis()
        );
    }

    private static InGamePlayerDto robberDto() {
        return new InGamePlayerDto(
            ROBBER_ID, ROBBER_NAME, PlayerRole.THIEF.name(), PlayerStatus.ACTIVE.name(),
            0, 0, 0, 0, System.currentTimeMillis()
        );
    }

    private static InGamePlayerDto arrestedRobberDto() {
        return new InGamePlayerDto(
            ROBBER_ID, ROBBER_NAME, PlayerRole.THIEF.name(), PlayerStatus.ARRESTED.name(),
            0, 0, 0, 0, System.currentTimeMillis()
        );
    }

    private static GemDto availableGemDto() {
        return new GemDto(
            GEM_ID, 37.0000, 127.0000, GemStatus.AVAILABLE.name(), null, System.currentTimeMillis()
        );
    }

    /** 보석과 동일한 좌표에 위치한 도둑 → 거리 0 */
    private static LocationDto robberAtGemLocation() {
        return new LocationDto(ROBBER_ID, 127.0000, 37.0000, System.currentTimeMillis());
    }

    // ────────────────────────────────────────────────────
    // startGame
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("startGame")
    class StartGame {

        private final RoomSettings settings = RoomSettings.defaultSettings();
        private final List<RoomPlayer> players = List.of(
            new RoomPlayer(COPS_ID, COPS_NAME, true, System.currentTimeMillis()),
            new RoomPlayer(ROBBER_ID, ROBBER_NAME, false, System.currentTimeMillis())
        );

        @Test
        @DisplayName("[성공] 정상 게임 시작 → gameId 반환, 역할 배정 이벤트 발행, 레지스트리 등록, 타이머 등록")
        void startGame_success() {
            when(gameStateStore.saveGameState(any())).thenReturn(new RepositoryResult.Found<>(null));
            when(inGamePlayerStore.savePlayer(anyString(), any())).thenReturn(new RepositoryResult.Found<>(null));

            var result = gameActionService.startGame(ROOM_ID, settings, players);

            assertInstanceOf(CommandResult.Success.class, result);
            var gameId = ((CommandResult.Success<String>) result).data();
            assertNotNull(gameId);
            assertFalse(gameId.isBlank());

            // 플레이어 수만큼 역할 배정 이벤트 발행
            verify(gameEventService, times(players.size())).publish(any(GameEvent.RoleAssigned.class));
            // 게임 시작 이벤트 1회 발행
            verify(gameEventService).publish(any(GameEvent.GameStarted.class));
            verify(gameRegistryStore).register(gameId);
            verify(gameTimerService).scheduleGame(eq(gameId), anyLong(), anyLong());
        }

        @Test
        @DisplayName("[실패] gameStateStore 저장 오류 → BusinessError, 역할 배정 없음")
        void startGame_storeError() {
            when(gameStateStore.saveGameState(any()))
                .thenReturn(new RepositoryResult.Error<>(new RuntimeException("Redis error")));

            var result = gameActionService.startGame(ROOM_ID, settings, players);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            verifyNoInteractions(inGamePlayerStore, gameEventService, gameTimerService, gameRegistryStore);
        }
    }

    // ────────────────────────────────────────────────────
    // arrest
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("arrest")
    class Arrest {

        private final ArrestCommand command = new ArrestCommand(GAME_ID, COPS_ID, ROBBER_ID);

        @Test
        @DisplayName("[성공] 범위 내 도둑 체포 → 도둑 ARRESTED, 경찰 arrestCount+1, 이벤트 발행")
        void arrest_success() {
            // 아직 체포되지 않은 다른 도둑이 존재 → 게임 종료 조건 미충족
            var activeRobber2 = new InGamePlayerDto(
                "robber-002", "Robber2", PlayerRole.THIEF.name(), PlayerStatus.ACTIVE.name(),
                0, 0, 0, 0, System.currentTimeMillis()
            );
            when(inGamePlayerStore.getPlayer(GAME_ID, COPS_ID)).thenReturn(new RepositoryResult.Found<>(copsDto()));
            when(inGamePlayerStore.getPlayer(GAME_ID, ROBBER_ID)).thenReturn(new RepositoryResult.Found<>(robberDto()));
            when(gameStateStore.getGameState(GAME_ID)).thenReturn(new RepositoryResult.Found<>(gameStateDto()));
            when(locationStore.getDistanceMeters(GAME_ID, COPS_ID, ROBBER_ID))
                .thenReturn(new RepositoryResult.Found<>(WITHIN_RANGE));
            when(inGamePlayerStore.updatePlayer(anyString(), any())).thenReturn(new RepositoryResult.Found<>(null));
            when(inGamePlayerStore.getAllPlayers(GAME_ID)).thenReturn(new RepositoryResult.Found<>(
                List.of(copsDto(), arrestedRobberDto(), activeRobber2)
            ));

            var result = gameActionService.arrest(command);

            assertInstanceOf(CommandResult.Success.class, result);

            var captor = ArgumentCaptor.forClass(InGamePlayerDto.class);
            verify(inGamePlayerStore, times(2)).updatePlayer(eq(GAME_ID), captor.capture());
            var updated = captor.getAllValues();

            // 도둑 → ARRESTED
            var updatedRobber = updated.stream()
                .filter(p -> p.playerId().equals(ROBBER_ID))
                .findFirst().orElseThrow();
            assertEquals(PlayerStatus.ARRESTED.name(), updatedRobber.status());

            // 경찰 arrestCount+1
            var updatedCops = updated.stream()
                .filter(p -> p.playerId().equals(COPS_ID))
                .findFirst().orElseThrow();
            assertEquals(1, updatedCops.arrestCount());

            verify(gameEventService).publish(any(GameEvent.PlayerArrested.class));
            // 도둑이 남아 있으므로 게임 종료 없음
            verify(gameTimerService, never()).endGame(anyString(), anyString());
        }

        @Test
        @DisplayName("[성공] 마지막 도둑 체포 → gameTimerService.endGame(POLICE) 호출")
        void arrest_lastRobber_endsGame() {
            when(inGamePlayerStore.getPlayer(GAME_ID, COPS_ID)).thenReturn(new RepositoryResult.Found<>(copsDto()));
            when(inGamePlayerStore.getPlayer(GAME_ID, ROBBER_ID)).thenReturn(new RepositoryResult.Found<>(robberDto()));
            when(gameStateStore.getGameState(GAME_ID)).thenReturn(new RepositoryResult.Found<>(gameStateDto()));
            when(locationStore.getDistanceMeters(GAME_ID, COPS_ID, ROBBER_ID))
                .thenReturn(new RepositoryResult.Found<>(WITHIN_RANGE));
            when(inGamePlayerStore.updatePlayer(anyString(), any())).thenReturn(new RepositoryResult.Found<>(null));
            // 도둑 1명만 존재하며 체포된 상태 → 모든 도둑 체포 완료
            when(inGamePlayerStore.getAllPlayers(GAME_ID)).thenReturn(new RepositoryResult.Found<>(
                List.of(copsDto(), arrestedRobberDto())
            ));

            gameActionService.arrest(command);

            verify(gameTimerService).endGame(GAME_ID, PlayerRole.POLICE.name());
        }

        @Test
        @DisplayName("[실패] 경찰 역할이 아닌 플레이어가 체포 시도 → BusinessError")
        void arrest_notCops() {
            var robberAsCops = new InGamePlayerDto(
                COPS_ID, COPS_NAME, PlayerRole.THIEF.name(), PlayerStatus.ACTIVE.name(),
                0, 0, 0, 0, System.currentTimeMillis()
            );
            when(inGamePlayerStore.getPlayer(GAME_ID, COPS_ID)).thenReturn(new RepositoryResult.Found<>(robberAsCops));

            var result = gameActionService.arrest(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals("Only cops can arrest", ((CommandResult.BusinessError<Void>) result).reason());
        }

        @Test
        @DisplayName("[실패] ACTIVE 상태가 아닌 경찰이 체포 시도 → BusinessError")
        void arrest_copsNotActive() {
            var inactiveCops = new InGamePlayerDto(
                COPS_ID, COPS_NAME, PlayerRole.POLICE.name(), PlayerStatus.ARRESTED.name(),
                0, 0, 0, 0, System.currentTimeMillis()
            );
            when(inGamePlayerStore.getPlayer(GAME_ID, COPS_ID)).thenReturn(new RepositoryResult.Found<>(inactiveCops));

            var result = gameActionService.arrest(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals("Cops player is not active", ((CommandResult.BusinessError<Void>) result).reason());
        }

        @Test
        @DisplayName("[실패] 타겟이 도둑 역할이 아님 → BusinessError")
        void arrest_targetNotRobber() {
            var copsAsTarget = new InGamePlayerDto(
                ROBBER_ID, ROBBER_NAME, PlayerRole.POLICE.name(), PlayerStatus.ACTIVE.name(),
                0, 0, 0, 0, System.currentTimeMillis()
            );
            when(inGamePlayerStore.getPlayer(GAME_ID, COPS_ID)).thenReturn(new RepositoryResult.Found<>(copsDto()));
            when(inGamePlayerStore.getPlayer(GAME_ID, ROBBER_ID)).thenReturn(new RepositoryResult.Found<>(copsAsTarget));

            var result = gameActionService.arrest(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals("Target is not a robber", ((CommandResult.BusinessError<Void>) result).reason());
        }

        @Test
        @DisplayName("[실패] 도둑이 이미 체포된 상태 → BusinessError")
        void arrest_robberAlreadyArrested() {
            when(inGamePlayerStore.getPlayer(GAME_ID, COPS_ID)).thenReturn(new RepositoryResult.Found<>(copsDto()));
            when(inGamePlayerStore.getPlayer(GAME_ID, ROBBER_ID)).thenReturn(new RepositoryResult.Found<>(arrestedRobberDto()));

            var result = gameActionService.arrest(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals("Robber is already arrested", ((CommandResult.BusinessError<Void>) result).reason());
        }

        @Test
        @DisplayName("[실패] 체포 사거리 초과 → BusinessError")
        void arrest_outOfRange() {
            when(inGamePlayerStore.getPlayer(GAME_ID, COPS_ID)).thenReturn(new RepositoryResult.Found<>(copsDto()));
            when(inGamePlayerStore.getPlayer(GAME_ID, ROBBER_ID)).thenReturn(new RepositoryResult.Found<>(robberDto()));
            when(gameStateStore.getGameState(GAME_ID)).thenReturn(new RepositoryResult.Found<>(gameStateDto()));
            when(locationStore.getDistanceMeters(GAME_ID, COPS_ID, ROBBER_ID))
                .thenReturn(new RepositoryResult.Found<>(OUT_OF_RANGE));

            var result = gameActionService.arrest(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertTrue(((CommandResult.BusinessError<Void>) result).reason().startsWith("Robber is out of arrest range"));
        }
    }

    // ────────────────────────────────────────────────────
    // rescue
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("rescue")
    class Rescue {

        private static final String RESCUER_ID = "robber-002";
        private static final String RESCUER_NAME = "RescuerPlayer";
        private final RescueCommand command = new RescueCommand(GAME_ID, RESCUER_ID, ROBBER_ID);

        private InGamePlayerDto rescuerDto() {
            return new InGamePlayerDto(
                RESCUER_ID, RESCUER_NAME, PlayerRole.THIEF.name(), PlayerStatus.ACTIVE.name(),
                0, 0, 0, 0, System.currentTimeMillis()
            );
        }

        @Test
        @DisplayName("[성공] 범위 내 체포된 동료 구출 → 동료 ACTIVE 복귀, 구출자 rescueCount+1, 이벤트 발행")
        void rescue_success() {
            when(inGamePlayerStore.getPlayer(GAME_ID, RESCUER_ID)).thenReturn(new RepositoryResult.Found<>(rescuerDto()));
            when(inGamePlayerStore.getPlayer(GAME_ID, ROBBER_ID)).thenReturn(new RepositoryResult.Found<>(arrestedRobberDto()));
            when(gameStateStore.getGameState(GAME_ID)).thenReturn(new RepositoryResult.Found<>(gameStateDto()));
            when(locationStore.getDistanceMeters(GAME_ID, RESCUER_ID, ROBBER_ID))
                .thenReturn(new RepositoryResult.Found<>(WITHIN_RANGE));
            when(inGamePlayerStore.updatePlayer(anyString(), any())).thenReturn(new RepositoryResult.Found<>(null));

            var result = gameActionService.rescue(command);

            assertInstanceOf(CommandResult.Success.class, result);

            var captor = ArgumentCaptor.forClass(InGamePlayerDto.class);
            verify(inGamePlayerStore, times(2)).updatePlayer(eq(GAME_ID), captor.capture());
            var updated = captor.getAllValues();

            // 체포된 동료 → ACTIVE 복귀
            var restoredRobber = updated.stream()
                .filter(p -> p.playerId().equals(ROBBER_ID))
                .findFirst().orElseThrow();
            assertEquals(PlayerStatus.ACTIVE.name(), restoredRobber.status());

            // 구출자 rescueCount+1
            var updatedRescuer = updated.stream()
                .filter(p -> p.playerId().equals(RESCUER_ID))
                .findFirst().orElseThrow();
            assertEquals(1, updatedRescuer.rescueCount());

            verify(gameEventService).publish(any(GameEvent.PlayerRescued.class));
        }

        @Test
        @DisplayName("[실패] 구출자가 도둑 역할이 아님 → BusinessError")
        void rescue_rescuerNotRobber() {
            var copsAsRescuer = new InGamePlayerDto(
                RESCUER_ID, RESCUER_NAME, PlayerRole.POLICE.name(), PlayerStatus.ACTIVE.name(),
                0, 0, 0, 0, System.currentTimeMillis()
            );
            when(inGamePlayerStore.getPlayer(GAME_ID, RESCUER_ID)).thenReturn(new RepositoryResult.Found<>(copsAsRescuer));

            var result = gameActionService.rescue(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals("Only robbers can rescue", ((CommandResult.BusinessError<Void>) result).reason());
        }

        @Test
        @DisplayName("[실패] 구출자가 ACTIVE 상태가 아님(체포됨) → BusinessError")
        void rescue_rescuerNotActive() {
            var arrestedRescuer = new InGamePlayerDto(
                RESCUER_ID, RESCUER_NAME, PlayerRole.THIEF.name(), PlayerStatus.ARRESTED.name(),
                0, 0, 0, 0, System.currentTimeMillis()
            );
            when(inGamePlayerStore.getPlayer(GAME_ID, RESCUER_ID)).thenReturn(new RepositoryResult.Found<>(arrestedRescuer));

            var result = gameActionService.rescue(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals("Rescuer is not active", ((CommandResult.BusinessError<Void>) result).reason());
        }

        @Test
        @DisplayName("[실패] 타겟이 체포 상태가 아님(이미 ACTIVE) → BusinessError")
        void rescue_targetNotArrested() {
            when(inGamePlayerStore.getPlayer(GAME_ID, RESCUER_ID)).thenReturn(new RepositoryResult.Found<>(rescuerDto()));
            when(inGamePlayerStore.getPlayer(GAME_ID, ROBBER_ID)).thenReturn(new RepositoryResult.Found<>(robberDto()));

            var result = gameActionService.rescue(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals("Target is not arrested", ((CommandResult.BusinessError<Void>) result).reason());
        }

        @Test
        @DisplayName("[실패] 구출 사거리 초과 → BusinessError")
        void rescue_outOfRange() {
            when(inGamePlayerStore.getPlayer(GAME_ID, RESCUER_ID)).thenReturn(new RepositoryResult.Found<>(rescuerDto()));
            when(inGamePlayerStore.getPlayer(GAME_ID, ROBBER_ID)).thenReturn(new RepositoryResult.Found<>(arrestedRobberDto()));
            when(gameStateStore.getGameState(GAME_ID)).thenReturn(new RepositoryResult.Found<>(gameStateDto()));
            when(locationStore.getDistanceMeters(GAME_ID, RESCUER_ID, ROBBER_ID))
                .thenReturn(new RepositoryResult.Found<>(OUT_OF_RANGE));

            var result = gameActionService.rescue(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertTrue(((CommandResult.BusinessError<Void>) result).reason().startsWith("Target is out of rescue range"));
        }
    }

    // ────────────────────────────────────────────────────
    // collectGem
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("collectGem")
    class CollectGem {

        private final CollectGemCommand command = new CollectGemCommand(GAME_ID, ROBBER_ID, GEM_ID);

        @Test
        @DisplayName("[성공] 범위 내 보석 획득 → 보석 COLLECTED, gemsCollected+1, 이벤트 발행")
        void collectGem_success() {
            when(inGamePlayerStore.getPlayer(GAME_ID, ROBBER_ID)).thenReturn(new RepositoryResult.Found<>(robberDto()));
            when(gemStore.getGem(GAME_ID, GEM_ID)).thenReturn(new RepositoryResult.Found<>(availableGemDto()));
            when(gameStateStore.getGameState(GAME_ID)).thenReturn(new RepositoryResult.Found<>(gameStateDto()));
            when(locationStore.getLocation(GAME_ID, ROBBER_ID)).thenReturn(new RepositoryResult.Found<>(robberAtGemLocation()));
            when(gemStore.updateGem(anyString(), any())).thenReturn(new RepositoryResult.Found<>(null));
            when(inGamePlayerStore.updatePlayer(anyString(), any())).thenReturn(new RepositoryResult.Found<>(null));

            var result = gameActionService.collectGem(command);

            assertInstanceOf(CommandResult.Success.class, result);

            // 보석 상태 → COLLECTED, collectedBy 설정
            var gemCaptor = ArgumentCaptor.forClass(GemDto.class);
            verify(gemStore).updateGem(eq(GAME_ID), gemCaptor.capture());
            assertEquals(GemStatus.COLLECTED.name(), gemCaptor.getValue().status());
            assertEquals(ROBBER_ID, gemCaptor.getValue().collectedBy());

            // gemsCollected+1
            var playerCaptor = ArgumentCaptor.forClass(InGamePlayerDto.class);
            verify(inGamePlayerStore).updatePlayer(eq(GAME_ID), playerCaptor.capture());
            assertEquals(1, playerCaptor.getValue().gemsCollected());

            verify(gameEventService).publish(any(GameEvent.GemCollected.class));
        }

        @Test
        @DisplayName("[실패] 도둑 역할이 아닌 플레이어가 보석 획득 시도 → BusinessError")
        void collectGem_notRobber() {
            var copsDto = new InGamePlayerDto(
                ROBBER_ID, ROBBER_NAME, PlayerRole.POLICE.name(), PlayerStatus.ACTIVE.name(),
                0, 0, 0, 0, System.currentTimeMillis()
            );
            when(inGamePlayerStore.getPlayer(GAME_ID, ROBBER_ID)).thenReturn(new RepositoryResult.Found<>(copsDto));

            var result = gameActionService.collectGem(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals("Only robbers can collect gems", ((CommandResult.BusinessError<Void>) result).reason());
        }

        @Test
        @DisplayName("[실패] ACTIVE 상태가 아닌 도둑의 보석 획득 시도 → BusinessError")
        void collectGem_playerNotActive() {
            when(inGamePlayerStore.getPlayer(GAME_ID, ROBBER_ID)).thenReturn(new RepositoryResult.Found<>(arrestedRobberDto()));

            var result = gameActionService.collectGem(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals("Player is not active", ((CommandResult.BusinessError<Void>) result).reason());
        }

        @Test
        @DisplayName("[실패] 이미 획득된 보석 → BusinessError")
        void collectGem_alreadyCollected() {
            var collectedGem = new GemDto(
                GEM_ID, 37.0, 127.0, GemStatus.COLLECTED.name(), "someone-else", System.currentTimeMillis()
            );
            when(inGamePlayerStore.getPlayer(GAME_ID, ROBBER_ID)).thenReturn(new RepositoryResult.Found<>(robberDto()));
            when(gemStore.getGem(GAME_ID, GEM_ID)).thenReturn(new RepositoryResult.Found<>(collectedGem));

            var result = gameActionService.collectGem(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals("Gem is already collected", ((CommandResult.BusinessError<Void>) result).reason());
        }

        @Test
        @DisplayName("[실패] 보석 획득 사거리 초과 → BusinessError")
        void collectGem_outOfRange() {
            when(inGamePlayerStore.getPlayer(GAME_ID, ROBBER_ID)).thenReturn(new RepositoryResult.Found<>(robberDto()));
            when(gemStore.getGem(GAME_ID, GEM_ID)).thenReturn(new RepositoryResult.Found<>(availableGemDto()));
            when(gameStateStore.getGameState(GAME_ID)).thenReturn(new RepositoryResult.Found<>(gameStateDto()));
            // 도둑 위치를 보석 좌표와 ~100km 떨어진 곳으로 설정
            var farLocation = new LocationDto(ROBBER_ID, 128.0, 38.0, System.currentTimeMillis());
            when(locationStore.getLocation(GAME_ID, ROBBER_ID)).thenReturn(new RepositoryResult.Found<>(farLocation));

            var result = gameActionService.collectGem(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertTrue(((CommandResult.BusinessError<Void>) result).reason().startsWith("Gem is out of collect range"));
        }
    }

    // ────────────────────────────────────────────────────
    // sendPing
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("sendPing")
    class SendPing {

        private final SendPingCommand command = new SendPingCommand(
            GAME_ID, COPS_ID, PingType.COPS_ROBBER_SPOTTED, 37.5, 127.0
        );

        @Test
        @DisplayName("[성공] 핑 전송 → PingAlert 이벤트 발행")
        void sendPing_success() {
            when(inGamePlayerStore.getPlayer(GAME_ID, COPS_ID)).thenReturn(new RepositoryResult.Found<>(copsDto()));

            var result = gameActionService.sendPing(command);

            assertInstanceOf(CommandResult.Success.class, result);

            var captor = ArgumentCaptor.forClass(GameEvent.PingAlert.class);
            verify(gameEventService).publish(captor.capture());
            assertEquals(GAME_ID, captor.getValue().gameId());
            assertEquals(COPS_ID, captor.getValue().senderId());
            assertEquals(PingType.COPS_ROBBER_SPOTTED.name(), captor.getValue().pingType());
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 플레이어가 핑 전송 → BusinessError, 이벤트 미발행")
        void sendPing_playerNotFound() {
            when(inGamePlayerStore.getPlayer(GAME_ID, COPS_ID))
                .thenReturn(new RepositoryResult.NotFound<>("Player not found"));

            var result = gameActionService.sendPing(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            verifyNoInteractions(gameEventService);
        }
    }
}
