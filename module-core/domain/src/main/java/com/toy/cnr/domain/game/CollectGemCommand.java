package com.toy.cnr.domain.game;

/** 보석 획득 커맨드 (도둑 전용) */
public record CollectGemCommand(
    String gameId,
    String robberId,
    String gemId
) {}
