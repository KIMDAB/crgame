package com.toy.cnr.port.game.model;

/**
 * 체포 이벤트 DTO (PostgreSQL 원장 저장용).
 *
 * @param id         PK (저장 전 null)
 * @param gameId     게임 세션 ID
 * @param robberId   체포된 도둑 ID
 * @param copsId     체포한 경찰 ID
 * @param arrestedAt 체포 시각 (epoch millis)
 * @param rescuedAt  구출 시각 (epoch millis) — null 이면 현재 수감 중
 */
public record ArrestEventDto(
    Long id,
    String gameId,
    String robberId,
    String copsId,
    long arrestedAt,
    Long rescuedAt
) {}
