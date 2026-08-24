package com.toy.cnr.rds.game.entity;

import com.toy.cnr.port.game.model.ArrestEventDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "arrest_event",
    indexes = @Index(name = "idx_arrest_event_game_robber", columnList = "game_id, robber_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArrestEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private String gameId;

    @Column(name = "robber_id", nullable = false)
    private String robberId;

    @Column(name = "cops_id", nullable = false)
    private String copsId;

    @Column(name = "arrested_at", nullable = false)
    private Long arrestedAt;

    @Column(name = "rescued_at")
    private Long rescuedAt;

    private ArrestEventEntity(String gameId, String robberId, String copsId, Long arrestedAt) {
        this.gameId = gameId;
        this.robberId = robberId;
        this.copsId = copsId;
        this.arrestedAt = arrestedAt;
    }

    public static ArrestEventEntity create(ArrestEventDto dto) {
        return new ArrestEventEntity(dto.gameId(), dto.robberId(), dto.copsId(), dto.arrestedAt());
    }

    public void markRescued(Long rescuedAt) {
        this.rescuedAt = rescuedAt;
    }

    public ArrestEventDto toDto() {
        return new ArrestEventDto(id, gameId, robberId, copsId, arrestedAt, rescuedAt);
    }
}
