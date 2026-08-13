package com.toy.cnr.domain.game;

/** 도둑 체포 커맨드 (경찰 전용) */
public record ArrestCommand(
    String gameId,
    String copsId,
    String robberId
) {}
