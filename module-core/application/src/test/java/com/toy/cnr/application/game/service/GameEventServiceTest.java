package com.toy.cnr.application.game.service;

import com.toy.cnr.domain.game.GameEvent;
import com.toy.cnr.domain.game.PlayerRole;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.GameEventPublisher;
import com.toy.cnr.port.game.GameEventSubscriber;
import com.toy.cnr.port.game.InGamePlayerStore;
import com.toy.cnr.port.game.model.GameEventDto;
import com.toy.cnr.port.game.model.InGamePlayerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GameEventServiceTest {

    private GameEventPublisher publisher;
    private GameEventSubscriber subscriber;
    private InGamePlayerStore inGamePlayerStore;
    private GameEventService gameEventService;

    @BeforeEach
    void setUp() {
        publisher = Mockito.mock(GameEventPublisher.class);
        subscriber = Mockito.mock(GameEventSubscriber.class);
        inGamePlayerStore = Mockito.mock(InGamePlayerStore.class);
        gameEventService = new GameEventService(publisher, subscriber, inGamePlayerStore);
    }

    private static final String GAME_ID = "game-001";
    private static final String PLAYER_ID = "player-001";
    private static final long TIMESTAMP = 1_000_000L;

    private static InGamePlayerDto playerDto(PlayerRole role) {
        return new InGamePlayerDto(PLAYER_ID, "TestPlayer", role.name(), "ACTIVE", 0, 0, 0, 0, TIMESTAMP);
    }

    // ────────────────────────────────────────────────────
    // publish
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("publish")
    class Publish {

        @Test
        @DisplayName("[성공] GameStarted 이벤트 발행 → publisher에 gameId와 GAME_STARTED 타입 DTO 전달")
        void publish_gameStarted() {
            var event = new GameEvent.GameStarted(GAME_ID, TIMESTAMP);

            gameEventService.publish(event);

            var captor = ArgumentCaptor.forClass(GameEventDto.class);
            verify(publisher).publish(eq(GAME_ID), captor.capture());
            assertEquals("GAME_STARTED", captor.getValue().type());
            assertEquals(GAME_ID, captor.getValue().gameId());
            assertEquals(TIMESTAMP, captor.getValue().timestamp());
        }

        @Test
        @DisplayName("[성공] PlayerArrested 이벤트 발행 → copsId, robberId 페이로드 포함")
        void publish_playerArrested() {
            var event = new GameEvent.PlayerArrested(GAME_ID, "cops-1", "robber-1", TIMESTAMP);

            gameEventService.publish(event);

            var captor = ArgumentCaptor.forClass(GameEventDto.class);
            verify(publisher).publish(eq(GAME_ID), captor.capture());
            var dto = captor.getValue();
            assertEquals("PLAYER_ARRESTED", dto.type());
            assertEquals("cops-1", dto.data().get("copsId"));
            assertEquals("robber-1", dto.data().get("robberId"));
        }

        @Test
        @DisplayName("[성공] GemCollected 이벤트 발행 → robberId, gemId 페이로드 포함")
        void publish_gemCollected() {
            var event = new GameEvent.GemCollected(GAME_ID, "robber-1", "gem-1", TIMESTAMP);

            gameEventService.publish(event);

            var captor = ArgumentCaptor.forClass(GameEventDto.class);
            verify(publisher).publish(eq(GAME_ID), captor.capture());
            var dto = captor.getValue();
            assertEquals("GEM_COLLECTED", dto.type());
            assertEquals("robber-1", dto.data().get("robberId"));
            assertEquals("gem-1", dto.data().get("gemId"));
        }

        @Test
        @DisplayName("[성공] GameEnded 이벤트 발행 → winnerRole 페이로드 포함")
        void publish_gameEnded() {
            var event = new GameEvent.GameEnded(GAME_ID, "POLICE", TIMESTAMP);

            gameEventService.publish(event);

            var captor = ArgumentCaptor.forClass(GameEventDto.class);
            verify(publisher).publish(eq(GAME_ID), captor.capture());
            assertEquals("GAME_ENDED", captor.getValue().type());
            assertEquals("POLICE", captor.getValue().data().get("winnerRole"));
        }
    }

    // ────────────────────────────────────────────────────
    // subscribe
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("subscribe")
    class Subscribe {

        @Test
        @DisplayName("[성공] subscribe 호출 → subscriber.subscribe에 gameId 전달, subscriberId 반환")
        void subscribe_returnsSubscriberId() {
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Found<>(playerDto(PlayerRole.POLICE)));
            when(subscriber.subscribe(eq(GAME_ID), any())).thenReturn("sub-001");

            var id = gameEventService.subscribe(GAME_ID, PLAYER_ID, event -> {});

            assertEquals("sub-001", id);
            verify(subscriber).subscribe(eq(GAME_ID), any());
        }

        @Test
        @DisplayName("[성공] 구독 콜백 — DTO 수신 시 도메인 GameEvent로 변환되어 onEvent 호출됨")
        void subscribe_callbackConvertsDto() {
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Found<>(playerDto(PlayerRole.POLICE)));

            @SuppressWarnings("unchecked")
            var consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
            when(subscriber.subscribe(eq(GAME_ID), consumerCaptor.capture())).thenReturn("sub-001");

            AtomicReference<GameEvent> received = new AtomicReference<>();
            gameEventService.subscribe(GAME_ID, PLAYER_ID, received::set);

            var dto = new GameEventDto(GAME_ID, "GAME_STARTED", Map.of(), TIMESTAMP);
            //noinspection unchecked
            consumerCaptor.getValue().accept(dto);

            assertNotNull(received.get());
            assertInstanceOf(GameEvent.GameStarted.class, received.get());
            assertEquals(GAME_ID, received.get().gameId());
        }

        @Test
        @DisplayName("[성공] PING_ALERT — 구독자와 같은 role의 핑은 전달됨")
        void subscribe_pingAlert_sameRole_delivered() {
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Found<>(playerDto(PlayerRole.POLICE)));

            @SuppressWarnings("unchecked")
            var consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
            when(subscriber.subscribe(eq(GAME_ID), consumerCaptor.capture())).thenReturn("sub-001");

            AtomicReference<GameEvent> received = new AtomicReference<>();
            gameEventService.subscribe(GAME_ID, PLAYER_ID, received::set);

            var dto = new GameEventDto(GAME_ID, "PING_ALERT",
                Map.of("senderId", "cops-1", "pingType", "POLICE_GATHER",
                    "latitude", "37.5", "longitude", "127.0"),
                TIMESTAMP);
            //noinspection unchecked
            consumerCaptor.getValue().accept(dto);

            assertNotNull(received.get());
            assertInstanceOf(GameEvent.PingAlert.class, received.get());
        }

        @Test
        @DisplayName("[성공] PING_ALERT — 구독자와 다른 role의 핑은 차단됨")
        void subscribe_pingAlert_differentRole_blocked() {
            when(inGamePlayerStore.getPlayer(GAME_ID, PLAYER_ID))
                .thenReturn(new RepositoryResult.Found<>(playerDto(PlayerRole.POLICE)));

            @SuppressWarnings("unchecked")
            var consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
            when(subscriber.subscribe(eq(GAME_ID), consumerCaptor.capture())).thenReturn("sub-001");

            AtomicReference<GameEvent> received = new AtomicReference<>();
            gameEventService.subscribe(GAME_ID, PLAYER_ID, received::set);

            var dto = new GameEventDto(GAME_ID, "PING_ALERT",
                Map.of("senderId", "robber-1", "pingType", "THIEF_DANGER",
                    "latitude", "37.5", "longitude", "127.0"),
                TIMESTAMP);
            //noinspection unchecked
            consumerCaptor.getValue().accept(dto);

            assertNull(received.get());
        }
    }

    // ────────────────────────────────────────────────────
    // unsubscribe
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("unsubscribe")
    class Unsubscribe {

        @Test
        @DisplayName("[성공] unsubscribe 호출 → subscriber.unsubscribe에 subscriberId 전달")
        void unsubscribe_delegatesToSubscriber() {
            gameEventService.unsubscribe("sub-001");

            verify(subscriber).unsubscribe("sub-001");
        }
    }
}
