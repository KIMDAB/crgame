package com.toy.cnr.port.game;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.model.GameStateDto;

/**
 * 게임 상태 저장/조회 포트 인터페이스.
 * <p>
 * Redis HASH를 통해 인게임 상태를 저장하고 조회합니다.
 */
public interface GameStateStore {

    RepositoryResult<Void> saveGameState(GameStateDto gameState);

    RepositoryResult<GameStateDto> getGameState(String gameId);

    RepositoryResult<Void> updateStatus(String gameId, String status);
}
