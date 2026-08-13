package com.toy.cnr.port.game;

import com.toy.cnr.port.common.RepositoryResult;

import java.util.Set;

/**
 * 활성 게임 ID 레지스트리 포트 인터페이스.
 * <p>
 * Redis SET "game:active"를 통해 진행 중인 게임 ID 목록을 관리합니다.
 * GemSpawnScheduler 등 주기적 작업에서 활성 게임 목록을 조회하는 데 사용합니다.
 */
public interface GameRegistryStore {

    RepositoryResult<Void> register(String gameId);

    RepositoryResult<Void> unregister(String gameId);

    RepositoryResult<Set<String>> getActiveGameIds();
}
