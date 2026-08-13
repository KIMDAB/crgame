package com.toy.cnr.api.room.usecase;

import com.toy.cnr.api.room.request.*;
import com.toy.cnr.api.room.response.*;
import com.toy.cnr.application.game.service.GameActionService;
import com.toy.cnr.application.room.service.RoomEventService;
import com.toy.cnr.application.room.service.RoomService;
import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.room.RoomEvent;
import org.springframework.stereotype.Component;

/**
 * 방(Room) 관련 비즈니스 오케스트레이션 유즈케이스.
 * <p>
 * HTTP 관심사를 모르며, CommandResult를 반환합니다.
 */
@Component
public class RoomUseCase {

    private final RoomService roomService;
    private final GameActionService gameActionService;
    private final RoomEventService roomEventService;

    public RoomUseCase(
        RoomService roomService,
        GameActionService gameActionService,
        RoomEventService roomEventService
    ) {
        this.roomService = roomService;
        this.gameActionService = gameActionService;
        this.roomEventService = roomEventService;
    }

    public CommandResult<RoomResponse> createRoom(RoomCreateRequest request, String playerId) {
        return roomService.createRoom(request.toCommand(playerId))
            .map(RoomResponse::from);
    }

    public CommandResult<RoomDetailResponse> getRoom(String roomId) {
        return roomService.getRoom(roomId)
            .map(RoomDetailResponse::from);
    }

    public CommandResult<RoomDetailResponse> updateSettings(
        String roomId,
        RoomUpdateSettingsRequest request,
        String requesterId
    ) {
        return roomService.updateSettings(request.toCommand(roomId, requesterId))
            .map(RoomDetailResponse::from);
    }

    public CommandResult<RoomDetailResponse> joinRoom(
        String roomId,
        RoomJoinRequest request,
        String playerId
    ) {
        var result = roomService.joinRoom(request.toCommand(roomId, playerId));
        if (result instanceof CommandResult.Success<com.toy.cnr.domain.room.Room> success) {
            var joinedPlayer = success.data().players().stream()
                .filter(p -> p.playerId().equals(playerId))
                .findFirst();
            joinedPlayer.ifPresent(player -> roomEventService.publish(
                new RoomEvent.PlayerJoined(roomId, playerId, player.playerName(), System.currentTimeMillis())
            ));
        }
        return result.map(RoomDetailResponse::from);
    }

    public CommandResult<Void> leaveRoom(String roomId, String playerId) {
        var result = roomService.leaveRoom(new com.toy.cnr.domain.room.RoomLeaveCommand(roomId, playerId));
        if (result instanceof CommandResult.Success<Void>) {
            roomEventService.publish(
                new RoomEvent.PlayerLeft(roomId, playerId, System.currentTimeMillis())
            );
        }
        return result;
    }

    public CommandResult<RoomPlayersResponse> getPlayers(String roomId) {
        return roomService.getPlayers(roomId)
            .map(RoomPlayersResponse::from);
    }

    public CommandResult<String> startGame(String roomId, String requesterId) {
        return roomService.startGame(new com.toy.cnr.domain.room.GameStartCommand(roomId, requesterId))
            .flatMap(room ->
                gameActionService.startGame(room.roomId(), room.settings(), room.players())
            );
    }
}
