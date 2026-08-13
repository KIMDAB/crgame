package com.toy.cnr.api.game.usecase;

import com.toy.cnr.api.game.response.*;
import com.toy.cnr.application.game.service.GameStateService;
import com.toy.cnr.domain.common.CommandResult;
import org.springframework.stereotype.Component;

/**
 * 게임 상태 조회 유즈케이스.
 */
@Component
public class GameStateUseCase {

    private final GameStateService gameStateService;

    public GameStateUseCase(GameStateService gameStateService) {
        this.gameStateService = gameStateService;
    }

    public CommandResult<GameStateResponse> getGameState(String gameId) {
        return gameStateService.getGameState(gameId)
            .map(GameStateResponse::from);
    }

    public CommandResult<InGamePlayersResponse> getPlayers(String gameId) {
        return gameStateService.getPlayers(gameId)
            .map(InGamePlayersResponse::from);
    }

    public CommandResult<InGamePlayerResponse> getPlayer(String gameId, String playerId) {
        return gameStateService.getPlayer(gameId, playerId)
            .map(InGamePlayerResponse::from);
    }

    public CommandResult<GameResultResponse> getResult(String gameId) {
        return gameStateService.getGameState(gameId).flatMap(state ->
            gameStateService.getPlayers(gameId).map(players ->
                GameResultResponse.from(state, players)
            )
        );
    }
}
