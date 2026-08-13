package com.toy.cnr.port.room.model;

/** 방 참가자 DTO */
public record RoomPlayerDto(
    String playerId,
    String playerName,
    boolean isHost,
    long joinedAt
) {}
