package com.toy.cnr.domain.game;

/** 동료 구출 커맨드 (도둑 전용) */
public record RescueCommand(
    String gameId,
    String rescuerId,
    String rescuedId
) {}
