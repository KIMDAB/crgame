package com.toy.cnr.port.game;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.model.GemDto;

import java.util.List;

/**
 * 보석 저장/조회 포트 인터페이스.
 * <p>
 * Redis HASH를 통해 인게임 보석 정보를 저장하고 조회합니다.
 */
public interface GemStore {

    RepositoryResult<Void> saveGem(String gameId, GemDto gem);

    RepositoryResult<GemDto> getGem(String gameId, String gemId);

    RepositoryResult<List<GemDto>> getAllGems(String gameId);

    RepositoryResult<Void> updateGem(String gameId, GemDto gem);
}
