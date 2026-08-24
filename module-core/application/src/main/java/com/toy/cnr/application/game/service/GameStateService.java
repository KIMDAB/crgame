package com.toy.cnr.application.game.service;

import com.toy.cnr.application.common.ResultMapper;
import com.toy.cnr.application.game.mapper.GameStateMapper;
import com.toy.cnr.application.game.mapper.InGamePlayerMapper;
import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.game.GameState;
import com.toy.cnr.domain.game.InGamePlayer;
import com.toy.cnr.domain.game.PlayerStatus;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.ArrestEventStore;
import com.toy.cnr.port.game.GameStateStore;
import com.toy.cnr.port.game.InGamePlayerStore;
import com.toy.cnr.port.game.model.ArrestEventDto;
import com.toy.cnr.port.game.model.InGamePlayerDto;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 게임 상태 조회 서비스.
 */
@Service
public class GameStateService {

    private final GameStateStore gameStateStore;
    private final InGamePlayerStore inGamePlayerStore;
    private final ArrestEventStore arrestEventStore;

    public GameStateService(
        GameStateStore gameStateStore,
        InGamePlayerStore inGamePlayerStore,
        ArrestEventStore arrestEventStore
    ) {
        this.gameStateStore = gameStateStore;
        this.inGamePlayerStore = inGamePlayerStore;
        this.arrestEventStore = arrestEventStore;
    }

    public CommandResult<GameState> getGameState(String gameId) {
        return ResultMapper.toCommandResult(gameStateStore.getGameState(gameId))
            .map(GameStateMapper::toDomain);
    }

    public CommandResult<List<InGamePlayer>> getPlayers(String gameId) {
        return ResultMapper.toCommandResult(inGamePlayerStore.getAllPlayers(gameId))
            .map(dtos -> dtos.stream()
                .map(dto -> InGamePlayerMapper.toDomain(dto, resolveArrestedBy(gameId, dto)))
                .toList()
            );
    }

    public CommandResult<InGamePlayer> getPlayer(String gameId, String playerId) {
        return ResultMapper.toCommandResult(inGamePlayerStore.getPlayer(gameId, playerId))
            .map(dto -> InGamePlayerMapper.toDomain(dto, resolveArrestedBy(gameId, dto)));
    }

    /** ARRESTED 상태인 경우에만 원장에서 체포한 경찰 ID를 조회합니다. */
    private String resolveArrestedBy(String gameId, InGamePlayerDto dto) {
        if (!PlayerStatus.ARRESTED.name().equals(dto.status())) {
            return null;
        }
        var result = arrestEventStore.findActiveArrest(gameId, dto.playerId());
        if (result instanceof RepositoryResult.Found<ArrestEventDto> found) {
            return found.data().copsId();
        }
        return null;
    }
}
