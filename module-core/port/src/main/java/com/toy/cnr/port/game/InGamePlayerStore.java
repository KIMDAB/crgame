package com.toy.cnr.port.game;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.model.InGamePlayerDto;

import java.util.List;

/**
 * 인게임 플레이어 저장/조회 포트 인터페이스.
 * <p>
 * Redis HASH를 통해 인게임 플레이어 정보를 저장하고 조회합니다.
 */
public interface InGamePlayerStore {

    RepositoryResult<Void> savePlayer(String gameId, InGamePlayerDto player);

    RepositoryResult<InGamePlayerDto> getPlayer(String gameId, String playerId);

    RepositoryResult<List<InGamePlayerDto>> getAllPlayers(String gameId);

    RepositoryResult<Void> updatePlayer(String gameId, InGamePlayerDto player);
}
