package com.toy.cnr.port.game.model;

/** 보석 DTO (Redis HASH 저장용) */
public record GemDto(
    String gemId,
    double latitude,
    double longitude,
    String status,
    String collectedBy,
    long spawnedAt
) {}
