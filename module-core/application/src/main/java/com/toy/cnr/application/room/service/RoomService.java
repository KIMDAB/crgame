package com.toy.cnr.application.room.service;

import com.toy.cnr.application.common.ResultMapper;
import com.toy.cnr.application.room.mapper.RoomMapper;
import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.room.*;
import com.toy.cnr.port.room.RoomStore;
import com.toy.cnr.port.room.model.RoomDto;
import com.toy.cnr.port.room.model.RoomPlayerDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 방(Room) 관리 서비스.
 * <p>
 * 방 생성/참가/나가기/설정 변경/게임 시작 비즈니스 로직을 담당합니다.
 */
@Service
public class RoomService {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ROOM_ID_LENGTH = 4;
    private static final Random RANDOM = new Random();

    private final RoomStore roomStore;

    public RoomService(RoomStore roomStore) {
        this.roomStore = roomStore;
    }

    public CommandResult<Room> createRoom(RoomCreateCommand command) {
        var roomId = generateRoomId();
        var host = new RoomPlayer(command.hostId(), command.hostName(), true, System.currentTimeMillis());
        var room = new Room(
            roomId,
            command.hostId(),
            RoomSettings.defaultSettings(),
            RoomStatus.WAITING,
            new ArrayList<>(List.of(host)),
            System.currentTimeMillis()
        );
        var saveResult = roomStore.saveRoom(RoomMapper.toDto(room));
        if (saveResult instanceof com.toy.cnr.port.common.RepositoryResult.Error<Void> e) {
            return new CommandResult.BusinessError<>(e.t().getMessage());
        }
        var addResult = roomStore.addPlayer(roomId, RoomMapper.toPlayerDto(host));
        if (addResult instanceof com.toy.cnr.port.common.RepositoryResult.Error<Void> e) {
            return new CommandResult.BusinessError<>(e.t().getMessage());
        }
        return new CommandResult.Success<>(room, "Room created: " + roomId);
    }

    /** 방 상세 조회 — settings.mapZone. */
    public CommandResult<Room> getRoom(String roomId) {
        return loadRoom(roomId);
    }

    /**
     * 방 설정 변경.
     * PUT /v1/rooms/{roomId}/settings 의 mapZone 이 Redis settings 에 저장됨.
     */
    public CommandResult<Room> updateSettings(RoomUpdateSettingsCommand command) {
        return loadRoom(command.roomId()).flatMap(room -> {
            if (!room.hostId().equals(command.requesterId())) {
                return new CommandResult.BusinessError<>("Only the host can update settings");
            }
            if (room.status() != RoomStatus.WAITING) {
                return new CommandResult.BusinessError<>("Settings can only be changed while waiting");
            }
            // mapZone 미전송 시 기존 게임 장소 유지 (인원·시간만 변경할 때)
            var mergedSettings = mergeSettings(room.settings(), command.settings());
            var updated = new Room(
                room.roomId(),
                room.hostId(),
                mergedSettings,
                room.status(),
                room.players(),
                room.createdAt()
            );
            var saveResult = roomStore.saveRoom(RoomMapper.toDto(updated));
            return ResultMapper.toCommandResult(saveResult).map(v -> updated);
        });
    }

    public CommandResult<Room> joinRoom(RoomJoinCommand command) {
        var roomResult = roomStore.getRoom(command.roomId());
        return ResultMapper.toCommandResult(roomResult).flatMap(dto -> {
            var room = RoomMapper.toDomain(dto);
            if (room.status() != RoomStatus.WAITING) {
                return new CommandResult.BusinessError<>("Room is not accepting players");
            }
            var settings = room.settings();
            if (room.players().size() >= settings.maxPlayers()) {
                return new CommandResult.BusinessError<>("Room is full");
            }
            boolean alreadyJoined = room.players().stream()
                .anyMatch(p -> p.playerId().equals(command.playerId()));
            if (alreadyJoined) {
                return new CommandResult.BusinessError<>("Player already in room");
            }
            var newPlayer = new RoomPlayer(
                command.playerId(),
                command.playerName(),
                false,
                System.currentTimeMillis()
            );
            var addResult = roomStore.addPlayer(command.roomId(), RoomMapper.toPlayerDto(newPlayer));
            return ResultMapper.toCommandResult(addResult).flatMap(v -> loadRoom(command.roomId()));
        });
    }

    public CommandResult<Void> leaveRoom(RoomLeaveCommand command) {
        var roomResult = roomStore.getRoom(command.roomId());
        return ResultMapper.toCommandResult(roomResult).flatMap(dto -> {
            var room = RoomMapper.toDomain(dto);
            boolean inRoom = room.players().stream()
                .anyMatch(p -> p.playerId().equals(command.playerId()));
            if (!inRoom) {
                return new CommandResult.BusinessError<>("Player not in room");
            }
            return ResultMapper.toCommandResult(roomStore.removePlayer(command.roomId(), command.playerId()));
        });
    }

    public CommandResult<List<RoomPlayer>> getPlayers(String roomId) {
        return ResultMapper.toCommandResult(roomStore.getPlayers(roomId))
            .map(dtos -> dtos.stream().map(RoomMapper::toPlayer).toList());
    }

    /**
     * 게임 시작 — 방 상태를 IN_GAME으로 변경 후 roomId 반환.
     * 실제 gameId 생성 및 역할 배정은 GameActionService에서 수행합니다.
     */
    public CommandResult<Room> startGame(GameStartCommand command) {
        var roomResult = roomStore.getRoom(command.roomId());
        return ResultMapper.toCommandResult(roomResult).flatMap(dto -> {
            var room = RoomMapper.toDomain(dto);
            if (!room.hostId().equals(command.requesterId())) {
                return new CommandResult.BusinessError<>("Only the host can start the game");
            }
            if (room.status() != RoomStatus.WAITING) {
                return new CommandResult.BusinessError<>("Game is already started or closed");
            }
            int minPlayers = room.settings().minPlayers();
            if (room.players().size() < minPlayers) {
                return new CommandResult.BusinessError<>(
                    "Not enough players. Required: " + minPlayers + ", current: " + room.players().size()
                );
            }
            var updateResult = roomStore.updateStatus(command.roomId(), RoomStatus.IN_GAME.name());
            return ResultMapper.toCommandResult(updateResult).flatMap(v -> loadRoom(command.roomId()));
        });
    }

    /**
     * Redis 방 메타(room:data) + 참가자 해시(room:players) 를 합쳐 Room 반환.
     * settings 에 저장된 mapZone 이 toDomain() 으로 복원됨.
     */
    private CommandResult<Room> loadRoom(String roomId) {
        return ResultMapper.toCommandResult(roomStore.getRoom(roomId))
            .flatMap(dto -> ResultMapper.toCommandResult(roomStore.getPlayers(roomId))
                .map(playerDtos -> {
                    var room = RoomMapper.toDomain(dto); // mapZone 포함
                    var players = playerDtos.stream().map(RoomMapper::toPlayer).toList();
                    return new Room(
                        room.roomId(),
                        room.hostId(),
                        room.settings(),
                        room.status(),
                        players,
                        room.createdAt()
                    );
                })
            );
    }

    /**
     * 설정 병합. incoming.mapZone == null 이면 기존 장소 유지, non-null 이면 새 좌표로 교체.
     */
    private static RoomSettings mergeSettings(RoomSettings existing, RoomSettings incoming) {
        var mapZone = incoming.mapZone() != null ? incoming.mapZone() : existing.mapZone();
        return new RoomSettings(
            incoming.gameMode(),
            incoming.minPlayers(),
            incoming.maxPlayers(),
            incoming.copsCount(),
            incoming.robbersCount(),
            incoming.gameDurationMinutes(),
            incoming.escapeTimeMinutes(),
            incoming.actionRadiusMeters(),
            mapZone
        );
    }

    private String generateRoomId() {
        var sb = new StringBuilder(ROOM_ID_LENGTH);
        for (int i = 0; i < ROOM_ID_LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
