package com.toy.cnr.rds.game;

import com.toy.cnr.rds.game.entity.ArrestEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArrestEventJpaRepository extends JpaRepository<ArrestEventEntity, Long> {

    Optional<ArrestEventEntity> findTopByGameIdAndRobberIdAndRescuedAtIsNullOrderByArrestedAtDesc(
        String gameId, String robberId
    );
}
