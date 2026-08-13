package com.toy.cnr.port.game.model;

/** 인게임 플레이어 DTO (Redis HASH 저장용) */
public record InGamePlayerDto(
    String playerId,
    String playerName,
    String role,
    String status,
    int arrestCount,
    int gemsCollected,
    int rescueCount,
    int escapeCount,
    long lastUpdatedAt
) {}
