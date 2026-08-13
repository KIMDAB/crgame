package com.toy.cnr.domain.room;

/** 방 참가자 정보 */
public record RoomPlayer(
    String playerId,
    String playerName,
    boolean isHost,
    long joinedAt
) {}
