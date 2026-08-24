package com.toy.cnr.rds.game;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.ArrestEventStore;
import com.toy.cnr.port.game.model.ArrestEventDto;
import com.toy.cnr.rds.game.entity.ArrestEventEntity;
import org.springframework.stereotype.Repository;

@Repository
public class ArrestEventRepositoryImpl implements ArrestEventStore {

    private final ArrestEventJpaRepository jpaRepository;

    public ArrestEventRepositoryImpl(ArrestEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RepositoryResult<ArrestEventDto> record(ArrestEventDto dto) {
        try {
            var saved = jpaRepository.save(ArrestEventEntity.create(dto));
            return new RepositoryResult.Found<>(saved.toDto());
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<ArrestEventDto> findActiveArrest(String gameId, String robberId) {
        try {
            return jpaRepository
                .findTopByGameIdAndRobberIdAndRescuedAtIsNullOrderByArrestedAtDesc(gameId, robberId)
                .map(e -> (RepositoryResult<ArrestEventDto>) new RepositoryResult.Found<>(e.toDto()))
                .orElse(new RepositoryResult.NotFound<>("No active arrest for robber: " + robberId));
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }

    @Override
    public RepositoryResult<Void> markRescued(String gameId, String robberId, long rescuedAt) {
        try {
            var entity = jpaRepository
                .findTopByGameIdAndRobberIdAndRescuedAtIsNullOrderByArrestedAtDesc(gameId, robberId)
                .orElse(null);
            if (entity == null) {
                return new RepositoryResult.NotFound<>("No active arrest to mark rescued");
            }
            entity.markRescued(rescuedAt);
            jpaRepository.save(entity);
            return new RepositoryResult.Found<>(null);
        } catch (Exception e) {
            return new RepositoryResult.Error<>(e);
        }
    }
}
