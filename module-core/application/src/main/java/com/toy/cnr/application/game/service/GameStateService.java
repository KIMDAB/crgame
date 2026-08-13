package com.toy.cnr.application.game.service;

import com.toy.cnr.application.common.ResultMapper;
import com.toy.cnr.application.game.mapper.GameStateMapper;
import com.toy.cnr.application.game.mapper.InGamePlayerMapper;
import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.game.GameState;
import com.toy.cnr.domain.game.InGamePlayer;
import com.toy.cnr.port.game.GameStateStore;
import com.toy.cnr.port.game.InGamePlayerStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 게임 상태 조회 서비스.
 */
@Service
public class GameStateService {

    private final GameStateStore gameStateStore;
    private final InGamePlayerStore inGamePlayerStore;

    public GameStateService(GameStateStore gameStateStore, InGamePlayerStore inGamePlayerStore) {
        this.gameStateStore = gameStateStore;
        this.inGamePlayerStore = inGamePlayerStore;
    }

    public CommandResult<GameState> getGameState(String gameId) {
        return ResultMapper.toCommandResult(gameStateStore.getGameState(gameId))
            .map(GameStateMapper::toDomain);
    }

    public CommandResult<List<InGamePlayer>> getPlayers(String gameId) {
        return ResultMapper.toCommandResult(inGamePlayerStore.getAllPlayers(gameId))
            .map(dtos -> dtos.stream().map(InGamePlayerMapper::toDomain).toList());
    }

    public CommandResult<InGamePlayer> getPlayer(String gameId, String playerId) {
        return ResultMapper.toCommandResult(inGamePlayerStore.getPlayer(gameId, playerId))
            .map(InGamePlayerMapper::toDomain);
    }
}
