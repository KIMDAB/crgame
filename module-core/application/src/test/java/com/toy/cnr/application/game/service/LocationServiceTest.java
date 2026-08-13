package com.toy.cnr.application.game.service;

import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.game.*;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.*;
import com.toy.cnr.port.game.model.GameStateDto;
import com.toy.cnr.port.game.model.InGamePlayerDto;
import com.toy.cnr.port.game.model.LocationDto;
import com.toy.cnr.port.room.model.RoomSettingsDto;
import com.toy.cnr.port.room.model.RoomSettingsDto.GeoPointDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LocationServiceTest {

    private LocationStore locationStore;
    private LocationPublisher locationPublisher;
    private LocationSubscriber locationSubscriber;
    private InGamePlayerStore inGamePlayerStore;
    private GameStateStore gameStateStore;
    private GameEventService gameEventService;
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        locationStore = Mockito.mock(LocationStore.class);
        locationPublisher = Mockito.mock(LocationPublisher.class);
        locationSubscriber = Mockito.mock(LocationSubscriber.class);
        inGamePlayerStore = Mockito.mock(InGamePlayerStore.class);
        gameStateStore = Mockito.mock(GameStateStore.class);
        gameEventService = Mockito.mock(GameEventService.class);
        locationService = new LocationService(
            locationStore, locationPublisher, locationSubscriber,
            inGamePlayerStore, gameStateStore, gameEventService
        );
    }

    // ────────────────────────────────────────────────────
    // Fixtures
    // ────────────────────────────────────────────────────

    private static final String GAME_ID = "game-001";
    private static final String PLAYER_ID = "player-001";

    /** 감옥 구역 — (37~38°, 127~128°) 정사각형 */
    private static final List<GeoPointDto> PRISON_POLYGON = List.of(
        new GeoPointDto(37.0, 127.0),
        new GeoPointDto(37.0, 128.0),
        new GeoPointDto(38.0, 128.0),
        new GeoPointDto(38.0, 127.0)
    );

    /** 제한/위험 구역 — (37~38°, 127~128°) 정사각형 (테스트용) */
    private static final List<GeoPointDto> RESTRICTED_POLYGON = List.of(
        new GeoPointDto(37.0, 127.0),
        new GeoPointDto(37.0, 128.0),
        new GeoPointDto(38.0, 128.0),
        new GeoPointDto(38.0, 127.0)
    );

    /** 감옥 내부 좌표 */
    private static final double LAT_INSIDE = 37.5;
    private static final double LON_INSIDE = 127.5;

    /** 감옥 외부 좌표 */
    private static final double LAT_OUTSIDE = 36.0;
    private static final double LON_OUTSIDE = 126.0;

    private static LocationPublishCommand commandAt(double lat, double lon) {
        return new LocationPublishCommand(GAME_ID, PLAYER_ID, lon, lat);
    }

    private static InGamePlayerDto activePlayerDto() {
        return new InGamePlayerDto(
            PLAYER_ID, "Player", PlayerRole.THIEF.name(), PlayerStatus.ACTIVE.name(),
            0, 0, 0, 0, System.currentTimeMillis()
        );
    }

    private static InGamePlayerDto arrestedPlayerDto() {
        return new InGamePlayerDto(
            PLAYER_ID, "Player", PlayerRole.THIEF.name(), PlayerStatus.ARRESTED.name(),
            0, 0, 0, 0, System.currentTimeMillis()
        );
    }

    private static GameStateDto gameStateDtoWith(List<GeoPointDto> prisonArea) {
        var settings = new RoomSettingsDto(
            "BASIC", 2, 10, 1, 1, 10, 1, 5.0,
            null, null, prisonArea, null
        );
        return new GameStateDto(
            GAME_ID, "ABCD", GameStatus.PLAYING.name(),
            settings, 1_000L, 601_000L
        );
    }

    private static GameStateDto gameStateDtoWithRestricted(List<GeoPointDto> restrictedArea) {
        var settings = new RoomSettingsDto(
            "BASIC", 2, 10, 1, 1, 10, 1, 5.0,
            null, null, null, restrictedArea
        );
        return new GameStateDto(
            GAME_ID, "ABCD", GameStatus.PLAYING.name(),
            settings, 1_000L, 601_000L
        );
    }

    // ────────────────────────────────────────────────────
    // publishLocation
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("publishLocation")
    class PublishLocation {

        @Test
        @DisplayName("[성공] 정상 좌표 발행 → locationStore 저장, publisher 호출, PlayerLocation 반환")
        void publishLocation_success() {
            var command = commandAt(LAT_INSIDE, LON_INSIDE);
            when(locationStore.saveLocation(GAME_ID, PLAYER_ID, LON_INSIDE, LAT_INSIDE))
                .thenReturn(new RepositoryResult.Found<>(null));
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Found<>(activePlayerDto()));

            var result = locationService.publishLocation(command);

            assertInstanceOf(CommandResult.Success.class, result);
            var location = ((CommandResult.Success<PlayerLocation>) result).data();
            assertEquals(GAME_ID, location.gameId());
            assertEquals(PLAYER_ID, location.playerId());
            assertEquals(LAT_INSIDE, location.latitude());
            assertEquals(LON_INSIDE, location.longitude());

            verify(locationStore).saveLocation(GAME_ID, PLAYER_ID, LON_INSIDE, LAT_INSIDE);
            verify(locationPublisher).publish(eq(GAME_ID), eq(PLAYER_ID), any(LocationDto.class));
        }

        @Test
        @DisplayName("[성공] publisher에 전달된 LocationDto의 좌표, playerId 검증")
        void publishLocation_publisherReceivesCorrectDto() {
            var command = commandAt(37.1, 127.2);
            when(locationStore.saveLocation(any(), any(), anyDouble(), anyDouble()))
                .thenReturn(new RepositoryResult.Found<>(null));
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Found<>(activePlayerDto()));

            locationService.publishLocation(command);

            var captor = ArgumentCaptor.forClass(LocationDto.class);
            verify(locationPublisher).publish(eq(GAME_ID), eq(PLAYER_ID), captor.capture());
            assertEquals(PLAYER_ID, captor.getValue().playerId());
            assertEquals(37.1, captor.getValue().latitude());
            assertEquals(127.2, captor.getValue().longitude());
        }

        @Test
        @DisplayName("[실패] locationStore.saveLocation 오류 → BusinessError, publisher 미호출")
        void publishLocation_storeError() {
            var command = commandAt(LAT_INSIDE, LON_INSIDE);
            when(locationStore.saveLocation(any(), any(), anyDouble(), anyDouble()))
                .thenReturn(new RepositoryResult.Error<>(new RuntimeException("Redis error")));

            var result = locationService.publishLocation(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            verifyNoInteractions(locationPublisher, gameEventService);
        }

        // ── checkPrisonEscape 시나리오 ──────────────────

        @Test
        @DisplayName("[성공] ACTIVE 플레이어 좌표 발행 → 감옥 이탈 검사 없음, PrisonEscapeWarning 미발행")
        void publishLocation_activePlayer_noPrisonCheck() {
            var command = commandAt(LAT_OUTSIDE, LON_OUTSIDE);
            when(locationStore.saveLocation(any(), any(), anyDouble(), anyDouble()))
                .thenReturn(new RepositoryResult.Found<>(null));
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Found<>(activePlayerDto()));

            locationService.publishLocation(command);

            // ACTIVE이므로 PrisonEscapeWarning은 발행되지 않는다.
            verify(gameEventService, never()).publish(any(GameEvent.PrisonEscapeWarning.class));
        }

        @Test
        @DisplayName("[성공] ARRESTED 플레이어, 감옥 내 위치 → PrisonEscapeWarning 미발행")
        void publishLocation_arrested_insidePrison_noWarning() {
            var command = commandAt(LAT_INSIDE, LON_INSIDE);
            when(locationStore.saveLocation(any(), any(), anyDouble(), anyDouble()))
                .thenReturn(new RepositoryResult.Found<>(null));
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Found<>(arrestedPlayerDto()));
            when(gameStateStore.getGameState(GAME_ID))
                .thenReturn(new RepositoryResult.Found<>(gameStateDtoWith(PRISON_POLYGON)));

            locationService.publishLocation(command);

            verify(gameEventService, never()).publish(any(GameEvent.PrisonEscapeWarning.class));
        }

        @Test
        @DisplayName("[성공] ARRESTED 플레이어, 감옥 이탈 → PrisonEscapeWarning 이벤트 발행")
        void publishLocation_arrested_outsidePrison_warningPublished() {
            var command = commandAt(LAT_OUTSIDE, LON_OUTSIDE);
            when(locationStore.saveLocation(any(), any(), anyDouble(), anyDouble()))
                .thenReturn(new RepositoryResult.Found<>(null));
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Found<>(arrestedPlayerDto()));
            when(gameStateStore.getGameState(GAME_ID))
                .thenReturn(new RepositoryResult.Found<>(gameStateDtoWith(PRISON_POLYGON)));

            locationService.publishLocation(command);

            var captor = ArgumentCaptor.forClass(GameEvent.PrisonEscapeWarning.class);
            verify(gameEventService).publish(captor.capture());
            assertEquals(GAME_ID, captor.getValue().gameId());
            assertEquals(PLAYER_ID, captor.getValue().playerId());
        }

        @Test
        @DisplayName("[성공] restrictedArea 외부 → 내부로 진입 시 RestrictedAreaEntered 이벤트 발행")
        void publishLocation_restrictedArea_enter_publishEvent() {
            when(locationStore.getLocation(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Found<>(
                    new LocationDto(PLAYER_ID, 126.0, 36.0, 1_000L)
                ));

            var command = commandAt(LAT_INSIDE, LON_INSIDE);
            when(locationStore.saveLocation(any(), any(), anyDouble(), anyDouble()))
                .thenReturn(new RepositoryResult.Found<>(null));
            when(gameStateStore.getGameState(GAME_ID))
                .thenReturn(new RepositoryResult.Found<>(gameStateDtoWithRestricted(RESTRICTED_POLYGON)));
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Found<>(activePlayerDto()));

            locationService.publishLocation(command);

            var captor = ArgumentCaptor.forClass(GameEvent.RestrictedAreaEntered.class);
            verify(gameEventService).publish(captor.capture());
            assertEquals(GAME_ID, captor.getValue().gameId());
            assertEquals(PLAYER_ID, captor.getValue().playerId());
            assertEquals(LAT_INSIDE, captor.getValue().latitude());
            assertEquals(LON_INSIDE, captor.getValue().longitude());
        }

        @Test
        @DisplayName("[성공] ARRESTED 플레이어, prisonArea 없음(null) → PrisonEscapeWarning 미발행")
        void publishLocation_arrested_noPrisonArea_noWarning() {
            var command = commandAt(LAT_OUTSIDE, LON_OUTSIDE);
            when(locationStore.saveLocation(any(), any(), anyDouble(), anyDouble()))
                .thenReturn(new RepositoryResult.Found<>(null));
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Found<>(arrestedPlayerDto()));
            when(gameStateStore.getGameState(GAME_ID))
                .thenReturn(new RepositoryResult.Found<>(gameStateDtoWith(null)));

            locationService.publishLocation(command);

            verify(gameEventService, never()).publish(any());
        }

        @Test
        @DisplayName("[성공] ARRESTED 플레이어, prisonArea 빈 리스트 → PrisonEscapeWarning 미발행")
        void publishLocation_arrested_emptyPrisonArea_noWarning() {
            var command = commandAt(LAT_OUTSIDE, LON_OUTSIDE);
            when(locationStore.saveLocation(any(), any(), anyDouble(), anyDouble()))
                .thenReturn(new RepositoryResult.Found<>(null));
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Found<>(arrestedPlayerDto()));
            when(gameStateStore.getGameState(GAME_ID))
                .thenReturn(new RepositoryResult.Found<>(gameStateDtoWith(List.of())));

            locationService.publishLocation(command);

            verify(gameEventService, never()).publish(any());
        }

        @Test
        @DisplayName("[성공] ARRESTED 플레이어, gameStateStore 오류 → PrisonEscapeWarning 미발행")
        void publishLocation_arrested_stateStoreError_noWarning() {
            var command = commandAt(LAT_OUTSIDE, LON_OUTSIDE);
            when(locationStore.saveLocation(any(), any(), anyDouble(), anyDouble()))
                .thenReturn(new RepositoryResult.Found<>(null));
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Found<>(arrestedPlayerDto()));
            when(gameStateStore.getGameState(GAME_ID))
                .thenReturn(new RepositoryResult.Error<>(new RuntimeException("Redis error")));

            locationService.publishLocation(command);

            verify(gameEventService, never()).publish(any());
        }

        @Test
        @DisplayName("[성공] inGamePlayerStore 조회 실패(NotFound) → 감옥 검사 건너뜀, 정상 완료")
        void publishLocation_playerNotFound_skipsCheck() {
            var command = commandAt(LAT_OUTSIDE, LON_OUTSIDE);
            when(locationStore.saveLocation(any(), any(), anyDouble(), anyDouble()))
                .thenReturn(new RepositoryResult.Found<>(null));
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.NotFound<>("Player not found"));

            var result = locationService.publishLocation(command);

            assertInstanceOf(CommandResult.Success.class, result);
            verify(gameEventService, never()).publish(any());
        }
    }

    // ────────────────────────────────────────────────────
    // subscribe
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("subscribe")
    class Subscribe {

        @Test
        @DisplayName("[성공] 여러 playerIds → 각 playerId마다 locationSubscriber.subscribe 호출")
        void subscribe_multiplePlayerIds() {
            var playerIds = List.of("player-1", "player-2", "player-3");

            locationService.subscribe(GAME_ID, playerIds, loc -> {});

            verify(locationSubscriber).subscribe(eq(GAME_ID), eq("player-1"), any());
            verify(locationSubscriber).subscribe(eq(GAME_ID), eq("player-2"), any());
            verify(locationSubscriber).subscribe(eq(GAME_ID), eq("player-3"), any());
            verify(locationSubscriber, times(3)).subscribe(eq(GAME_ID), anyString(), any());
        }

        @Test
        @DisplayName("[성공] 빈 playerIds → locationSubscriber.subscribe 미호출")
        void subscribe_emptyPlayerIds_noSubscription() {
            locationService.subscribe(GAME_ID, List.of(), loc -> {});

            verifyNoInteractions(locationSubscriber);
        }

        @Test
        @DisplayName("[성공] 구독 콜백 — LocationDto 수신 시 gameId가 포함된 PlayerLocation으로 변환됨")
        void subscribe_callbackConvertsDtoToDomain() {
            @SuppressWarnings("unchecked")
            var consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
            doNothing().when(locationSubscriber).subscribe(eq(GAME_ID), eq(PLAYER_ID), consumerCaptor.capture());

            AtomicReference<PlayerLocation> received = new AtomicReference<>();
            locationService.subscribe(GAME_ID, List.of(PLAYER_ID), received::set);

            // Simulate subscriber receiving a raw LocationDto
            var dto = new LocationDto(PLAYER_ID, 127.5, 37.5, 1_000L);
            //noinspection unchecked
            consumerCaptor.getValue().accept(dto);

            assertNotNull(received.get());
            assertEquals(GAME_ID, received.get().gameId());
            assertEquals(PLAYER_ID, received.get().playerId());
            assertEquals(37.5, received.get().latitude());
            assertEquals(127.5, received.get().longitude());
        }
    }

    // ────────────────────────────────────────────────────
    // unsubscribe
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("unsubscribe")
    class Unsubscribe {

        @Test
        @DisplayName("[성공] 여러 playerIds → 각 playerId마다 locationSubscriber.unsubscribe 호출")
        void unsubscribe_multiplePlayerIds() {
            var playerIds = List.of("player-1", "player-2");

            locationService.unsubscribe(GAME_ID, playerIds);

            verify(locationSubscriber).unsubscribe(GAME_ID, "player-1");
            verify(locationSubscriber).unsubscribe(GAME_ID, "player-2");
            verify(locationSubscriber, times(2)).unsubscribe(eq(GAME_ID), anyString());
        }

        @Test
        @DisplayName("[성공] 빈 playerIds → locationSubscriber.unsubscribe 미호출")
        void unsubscribe_emptyPlayerIds_noCall() {
            locationService.unsubscribe(GAME_ID, List.of());

            verifyNoInteractions(locationSubscriber);
        }
    }
}
