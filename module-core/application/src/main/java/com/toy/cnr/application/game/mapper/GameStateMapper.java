package com.toy.cnr.application.game.mapper;

import com.toy.cnr.application.room.mapper.RoomMapper;
import com.toy.cnr.domain.game.GameState;
import com.toy.cnr.domain.game.GameStatus;
import com.toy.cnr.port.game.model.GameStateDto;

public final class GameStateMapper {

    private GameStateMapper() {}

    public static GameStateDto toDto(GameState state) {
        return new GameStateDto(
            state.gameId(),
            state.roomId(),
            state.status().name(),
            RoomMapper.toSettingsDto(state.settings()),
            state.startedAt(),
            state.endsAt()
        );
    }

    public static GameState toDomain(GameStateDto dto) {
        return new GameState(
            dto.gameId(),
            dto.roomId(),
            GameStatus.valueOf(dto.status()),
            RoomMapper.toSettings(dto.settings()),
            dto.startedAt(),
            dto.endsAt()
        );
    }
}
