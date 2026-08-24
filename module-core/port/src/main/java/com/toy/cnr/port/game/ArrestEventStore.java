package com.toy.cnr.port.game;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.model.ArrestEventDto;

/**
 * 체포 이벤트 원장 포트.
 * PostgreSQL에 체포/구출 이력을 영속화합니다.
 */
public interface ArrestEventStore {

    RepositoryResult<ArrestEventDto> record(ArrestEventDto dto);

    /** robberId 기준 현재 수감 중인(rescuedAt = null) 가장 최근 체포 이벤트 조회 */
    RepositoryResult<ArrestEventDto> findActiveArrest(String gameId, String robberId);

    /** 수감 중인 이벤트에 rescuedAt 을 기록합니다. */
    RepositoryResult<Void> markRescued(String gameId, String robberId, long rescuedAt);
}
