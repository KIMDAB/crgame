package com.toy.cnr.application.room.service;

import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.room.*;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.room.RoomStore;
import com.toy.cnr.port.room.model.RoomDto;
import com.toy.cnr.port.room.model.RoomPlayerDto;
import com.toy.cnr.port.room.model.RoomSettingsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class RoomServiceTest {

    private RoomStore roomStore;
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomStore = Mockito.mock(RoomStore.class);
        roomService = new RoomService(roomStore);
    }

    // ────────────────────────────────────────────────────
    // Fixtures
    // ────────────────────────────────────────────────────

    private static final String HOST_ID = "host-001";
    private static final String HOST_NAME = "HostPlayer";
    private static final String PLAYER_ID = "player-001";
    private static final String PLAYER_NAME = "GuestPlayer";
    private static final String ROOM_ID = "ABCD";

    private static RoomSettingsDto defaultSettingsDto() {
        return new RoomSettingsDto(
            "BASIC", 2, 10, 1, 1, 10, 1, 1.0,
            null, null, null, null
        );
    }

    private static RoomPlayerDto hostPlayerDto() {
        return new RoomPlayerDto(HOST_ID, HOST_NAME, true, System.currentTimeMillis());
    }

    private static RoomDto waitingRoomDto() {
        return new RoomDto(
            ROOM_ID, HOST_ID,
            defaultSettingsDto(),
            "WAITING",
            new ArrayList<>(List.of(hostPlayerDto())),
            System.currentTimeMillis()
        );
    }

    private static RoomDto inGameRoomDto() {
        return new RoomDto(
            ROOM_ID, HOST_ID,
            defaultSettingsDto(),
            "IN_GAME",
            new ArrayList<>(List.of(hostPlayerDto())),
            System.currentTimeMillis()
        );
    }

    private void stubPlayersFromRoom(RoomDto dto) {
        when(roomStore.getPlayers(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(dto.players()));
    }

    private static RoomSettingsDto settingsDtoWithMapZone() {
        return new RoomSettingsDto(
            "BASIC", 2, 10, 1, 1, 10, 1, 1.0,
            new RoomSettingsDto.GeoPointDto(37.4979, 127.0276),
            List.of(new RoomSettingsDto.GeoPointDto(37.4980, 127.0280)),
            null,
            null
        );
    }

    private static RoomDto fullRoomDto() {
        // maxPlayers=10, players already at max
        List<RoomPlayerDto> players = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            players.add(new RoomPlayerDto("player-" + i, "Player" + i, i == 0, System.currentTimeMillis()));
        }
        return new RoomDto(ROOM_ID, HOST_ID, defaultSettingsDto(), "WAITING", players, System.currentTimeMillis());
    }

    // ────────────────────────────────────────────────────
    // createRoom
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("createRoom")
    class CreateRoom {

        @Test
        @DisplayName("[성공] 방 생성 시 4글자 roomId가 생성되고 방장이 players에 포함된다")
        void createRoom_success() {
            when(roomStore.saveRoom(any())).thenReturn(new RepositoryResult.Found<>(null));
            when(roomStore.addPlayer(anyString(), any())).thenReturn(new RepositoryResult.Found<>(null));

            var result = roomService.createRoom(new RoomCreateCommand(HOST_ID, HOST_NAME));

            assertInstanceOf(CommandResult.Success.class, result);
            var room = ((CommandResult.Success<Room>) result).data();
            assertEquals(4, room.roomId().length());
            assertTrue(room.roomId().matches("[A-Z0-9]{4}"));
            assertEquals(HOST_ID, room.hostId());
            assertEquals(1, room.players().size());
            assertEquals(HOST_ID, room.players().get(0).playerId());
            assertTrue(room.players().get(0).isHost());
        }

        @Test
        @DisplayName("[실패] saveRoom 저장소 오류 → BusinessError")
        void createRoom_saveRoomError() {
            when(roomStore.saveRoom(any()))
                .thenReturn(new RepositoryResult.Error<>(new RuntimeException("Redis error")));

            var result = roomService.createRoom(new RoomCreateCommand(HOST_ID, HOST_NAME));

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }

        @Test
        @DisplayName("[실패] addPlayer 저장소 오류 → BusinessError")
        void createRoom_addPlayerError() {
            when(roomStore.saveRoom(any())).thenReturn(new RepositoryResult.Found<>(null));
            when(roomStore.addPlayer(anyString(), any()))
                .thenReturn(new RepositoryResult.Error<>(new RuntimeException("Redis error")));

            var result = roomService.createRoom(new RoomCreateCommand(HOST_ID, HOST_NAME));

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }
    }

    // ────────────────────────────────────────────────────
    // getRoom
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("getRoom")
    class GetRoom {

        @Test
        @DisplayName("[성공] 정상 조회 → 올바른 Room 반환")
        void getRoom_success() {
            var dto = waitingRoomDto();
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(dto));
            stubPlayersFromRoom(dto);

            var result = roomService.getRoom(ROOM_ID);

            assertInstanceOf(CommandResult.Success.class, result);
            var room = ((CommandResult.Success<Room>) result).data();
            assertEquals(ROOM_ID, room.roomId());
            assertEquals(HOST_ID, room.hostId());
            assertEquals(RoomStatus.WAITING, room.status());
        }

        @Test
        @DisplayName("[성공] mapZone이 저장된 방 → settings.mapZone 반환")
        void getRoom_withMapZone_returnsMapZone() {
            var dto = new RoomDto(
                ROOM_ID, HOST_ID, settingsDtoWithMapZone(), "WAITING",
                List.of(hostPlayerDto()), System.currentTimeMillis()
            );
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(dto));
            stubPlayersFromRoom(dto);

            var result = roomService.getRoom(ROOM_ID);

            assertInstanceOf(CommandResult.Success.class, result);
            var zone = ((CommandResult.Success<Room>) result).data().settings().mapZone();
            assertNotNull(zone);
            assertEquals(37.4979, zone.rallyPoint().latitude());
            assertEquals(1, zone.playArea().size());
        }

        @Test
        @DisplayName("[실패] 방 없음 (NotFound) → BusinessError")
        void getRoom_notFound() {
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.NotFound<>("Room not found"));

            var result = roomService.getRoom(ROOM_ID);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }

        @Test
        @DisplayName("[실패] 저장소 오류 (Error) → BusinessError")
        void getRoom_storeError() {
            when(roomStore.getRoom(ROOM_ID))
                .thenReturn(new RepositoryResult.Error<>(new RuntimeException("Redis error")));

            var result = roomService.getRoom(ROOM_ID);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }
    }

    // ────────────────────────────────────────────────────
    // updateSettings
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateSettings")
    class UpdateSettings {

        private final RoomSettings newSettings = new RoomSettings(
            GameMode.BASIC, 3, 8, 2, 2, 15, 2, 2.0, null
        );

        @Test
        @DisplayName("[성공] 방장이 WAITING 상태에서 설정 변경")
        void updateSettings_success() {
            var dto = waitingRoomDto();
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(dto));
            stubPlayersFromRoom(dto);
            when(roomStore.saveRoom(any())).thenReturn(new RepositoryResult.Found<>(null));

            var command = new RoomUpdateSettingsCommand(ROOM_ID, HOST_ID, newSettings);
            var result = roomService.updateSettings(command);

            assertInstanceOf(CommandResult.Success.class, result);
            var room = ((CommandResult.Success<Room>) result).data();
            assertEquals(15, room.settings().gameDurationMinutes());
        }

        @Test
        @DisplayName("[성공] mapZone 없이 다른 설정만 변경 → 기존 mapZone 유지")
        void updateSettings_withoutMapZone_preservesExistingMapZone() {
            var dto = new RoomDto(
                ROOM_ID, HOST_ID, settingsDtoWithMapZone(), "WAITING",
                List.of(hostPlayerDto()), System.currentTimeMillis()
            );
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(dto));
            stubPlayersFromRoom(dto);
            when(roomStore.saveRoom(any())).thenReturn(new RepositoryResult.Found<>(null));

            var incoming = new RoomSettings(GameMode.BASIC, 3, 8, 2, 2, 15, 2, 2.0, null);
            var result = roomService.updateSettings(new RoomUpdateSettingsCommand(ROOM_ID, HOST_ID, incoming));

            assertInstanceOf(CommandResult.Success.class, result);
            var zone = ((CommandResult.Success<Room>) result).data().settings().mapZone();
            assertNotNull(zone);
            assertEquals(37.4979, zone.rallyPoint().latitude());
        }

        @Test
        @DisplayName("[성공] mapZone 포함 설정 변경 → mapZone 저장")
        void updateSettings_withMapZone_savesMapZone() {
            var dto = waitingRoomDto();
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(dto));
            stubPlayersFromRoom(dto);
            when(roomStore.saveRoom(any())).thenReturn(new RepositoryResult.Found<>(null));

            var zone = new MapZone(
                new GeoPoint(37.5, 127.0),
                List.of(new GeoPoint(37.51, 127.01)),
                List.of(),
                List.of()
            );
            var incoming = new RoomSettings(GameMode.BASIC, 2, 10, 1, 1, 10, 1, 1.0, zone);
            var result = roomService.updateSettings(new RoomUpdateSettingsCommand(ROOM_ID, HOST_ID, incoming));

            assertInstanceOf(CommandResult.Success.class, result);
            var saved = ((CommandResult.Success<Room>) result).data().settings().mapZone();
            assertNotNull(saved);
            assertEquals(37.5, saved.rallyPoint().latitude());
            assertEquals(1, saved.playArea().size());
        }

        @Test
        @DisplayName("[실패] 방장이 아닌 사람이 변경 시도 → BusinessError")
        void updateSettings_notHost() {
            var dto = waitingRoomDto();
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(dto));
            stubPlayersFromRoom(dto);

            var command = new RoomUpdateSettingsCommand(ROOM_ID, "other-player", newSettings);
            var result = roomService.updateSettings(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals(
                "Only the host can update settings",
                ((CommandResult.BusinessError<Room>) result).reason()
            );
        }

        @Test
        @DisplayName("[실패] IN_GAME 상태에서 변경 시도 → BusinessError")
        void updateSettings_inGameStatus() {
            var dto = inGameRoomDto();
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(dto));
            stubPlayersFromRoom(dto);

            var command = new RoomUpdateSettingsCommand(ROOM_ID, HOST_ID, newSettings);
            var result = roomService.updateSettings(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals(
                "Settings can only be changed while waiting",
                ((CommandResult.BusinessError<Room>) result).reason()
            );
        }

        @Test
        @DisplayName("[실패] 방 없음 → BusinessError")
        void updateSettings_roomNotFound() {
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.NotFound<>("Room not found"));

            var command = new RoomUpdateSettingsCommand(ROOM_ID, HOST_ID, newSettings);
            var result = roomService.updateSettings(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }
    }

    // ────────────────────────────────────────────────────
    // joinRoom
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("joinRoom")
    class JoinRoom {

        private final RoomJoinCommand command = new RoomJoinCommand(ROOM_ID, PLAYER_ID, PLAYER_NAME);

        @Test
        @DisplayName("[성공] 정상 입장 → 플레이어가 추가된 Room 반환")
        void joinRoom_success() {
            var playersAfterJoin = List.of(
                hostPlayerDto(),
                new RoomPlayerDto(PLAYER_ID, PLAYER_NAME, false, System.currentTimeMillis())
            );
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(waitingRoomDto()));
            when(roomStore.addPlayer(eq(ROOM_ID), any())).thenReturn(new RepositoryResult.Found<>(null));
            when(roomStore.getPlayers(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(playersAfterJoin));

            var result = roomService.joinRoom(command);

            assertInstanceOf(CommandResult.Success.class, result);
            var room = ((CommandResult.Success<Room>) result).data();
            assertEquals(2, room.players().size());
        }

        @Test
        @DisplayName("[실패] IN_GAME 방 입장 시도 → BusinessError")
        void joinRoom_roomNotWaiting() {
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(inGameRoomDto()));

            var result = roomService.joinRoom(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals("Room is not accepting players", ((CommandResult.BusinessError<Room>) result).reason());
        }

        @Test
        @DisplayName("[실패] 정원 초과 → BusinessError")
        void joinRoom_roomFull() {
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(fullRoomDto()));

            var result = roomService.joinRoom(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals("Room is full", ((CommandResult.BusinessError<Room>) result).reason());
        }

        @Test
        @DisplayName("[실패] 이미 입장한 플레이어 → BusinessError")
        void joinRoom_alreadyJoined() {
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(waitingRoomDto()));

            var duplicateCommand = new RoomJoinCommand(ROOM_ID, HOST_ID, HOST_NAME);
            var result = roomService.joinRoom(duplicateCommand);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals("Player already in room", ((CommandResult.BusinessError<Room>) result).reason());
        }

        @Test
        @DisplayName("[실패] 방 없음 → BusinessError")
        void joinRoom_roomNotFound() {
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.NotFound<>("Room not found"));

            var result = roomService.joinRoom(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }
    }

    // ────────────────────────────────────────────────────
    // leaveRoom
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("leaveRoom")
    class LeaveRoom {

        @Test
        @DisplayName("[성공] 정상 퇴장 → Success")
        void leaveRoom_success() {
            var roomWithTwoPlayers = new RoomDto(
                ROOM_ID, HOST_ID, defaultSettingsDto(), "WAITING",
                List.of(
                    hostPlayerDto(),
                    new RoomPlayerDto(PLAYER_ID, PLAYER_NAME, false, System.currentTimeMillis())
                ),
                System.currentTimeMillis()
            );
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(roomWithTwoPlayers));
            when(roomStore.removePlayer(ROOM_ID, PLAYER_ID)).thenReturn(new RepositoryResult.Found<>(null));

            var result = roomService.leaveRoom(new RoomLeaveCommand(ROOM_ID, PLAYER_ID));

            assertInstanceOf(CommandResult.Success.class, result);
        }

        @Test
        @DisplayName("[실패] 방에 없는 플레이어 → BusinessError")
        void leaveRoom_playerNotInRoom() {
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(waitingRoomDto()));

            var result = roomService.leaveRoom(new RoomLeaveCommand(ROOM_ID, "unknown-player"));

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals("Player not in room", ((CommandResult.BusinessError<Void>) result).reason());
        }

        @Test
        @DisplayName("[실패] 방 없음 → BusinessError")
        void leaveRoom_roomNotFound() {
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.NotFound<>("Room not found"));

            var result = roomService.leaveRoom(new RoomLeaveCommand(ROOM_ID, PLAYER_ID));

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
        @DisplayName("[성공] 플레이어 목록 조회 → 올바른 목록 반환")
        void getPlayers_success() {
            var players = List.of(
                hostPlayerDto(),
                new RoomPlayerDto(PLAYER_ID, PLAYER_NAME, false, System.currentTimeMillis())
            );
            when(roomStore.getPlayers(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(players));

            var result = roomService.getPlayers(ROOM_ID);

            assertInstanceOf(CommandResult.Success.class, result);
            var list = ((CommandResult.Success<List<RoomPlayer>>) result).data();
            assertEquals(2, list.size());
            assertEquals(HOST_ID, list.get(0).playerId());
        }

        @Test
        @DisplayName("[실패] 방 없음 → BusinessError")
        void getPlayers_notFound() {
            when(roomStore.getPlayers(ROOM_ID)).thenReturn(new RepositoryResult.NotFound<>("Room not found"));

            var result = roomService.getPlayers(ROOM_ID);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }
    }

    // ────────────────────────────────────────────────────
    // startGame
    // ────────────────────────────────────────────────────

    @Nested
    @DisplayName("startGame")
    class StartGame {

        private final GameStartCommand command = new GameStartCommand(ROOM_ID, HOST_ID);

        @Test
        @DisplayName("[성공] 방장 + 최소 인원 충족 + WAITING → status IN_GAME")
        void startGame_success() {
            // minPlayers=2, 현재 2명(host+player)
            var twoPlayerRoom = new RoomDto(
                ROOM_ID, HOST_ID, defaultSettingsDto(), "WAITING",
                List.of(
                    hostPlayerDto(),
                    new RoomPlayerDto(PLAYER_ID, PLAYER_NAME, false, System.currentTimeMillis())
                ),
                System.currentTimeMillis()
            );
            var inGameRoom = new RoomDto(
                ROOM_ID, HOST_ID, defaultSettingsDto(), "IN_GAME",
                twoPlayerRoom.players(),
                System.currentTimeMillis()
            );
            when(roomStore.getRoom(ROOM_ID))
                .thenReturn(new RepositoryResult.Found<>(twoPlayerRoom))
                .thenReturn(new RepositoryResult.Found<>(inGameRoom));
            when(roomStore.getPlayers(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(twoPlayerRoom.players()));
            when(roomStore.updateStatus(ROOM_ID, "IN_GAME")).thenReturn(new RepositoryResult.Found<>(null));

            var result = roomService.startGame(command);

            assertInstanceOf(CommandResult.Success.class, result);
            var room = ((CommandResult.Success<Room>) result).data();
            assertEquals(RoomStatus.IN_GAME, room.status());
        }

        @Test
        @DisplayName("[실패] 방장이 아닌 사람이 시작 시도 → BusinessError")
        void startGame_notHost() {
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(waitingRoomDto()));

            var result = roomService.startGame(new GameStartCommand(ROOM_ID, "other-player"));

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals(
                "Only the host can start the game",
                ((CommandResult.BusinessError<Room>) result).reason()
            );
        }

        @Test
        @DisplayName("[실패] 이미 IN_GAME 상태 → BusinessError")
        void startGame_alreadyStarted() {
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(inGameRoomDto()));

            var result = roomService.startGame(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertEquals(
                "Game is already started or closed",
                ((CommandResult.BusinessError<Room>) result).reason()
            );
        }

        @Test
        @DisplayName("[실패] 최소 인원 미달 → BusinessError")
        void startGame_notEnoughPlayers() {
            // WAITING 방에 방장 1명만 있음 (minPlayers=2)
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.Found<>(waitingRoomDto()));

            var result = roomService.startGame(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
            assertTrue(((CommandResult.BusinessError<Room>) result).reason().startsWith("Not enough players"));
        }

        @Test
        @DisplayName("[실패] 방 없음 → BusinessError")
        void startGame_roomNotFound() {
            when(roomStore.getRoom(ROOM_ID)).thenReturn(new RepositoryResult.NotFound<>("Room not found"));

            var result = roomService.startGame(command);

            assertInstanceOf(CommandResult.BusinessError.class, result);
        }
    }
}
