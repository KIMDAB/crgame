package com.toy.cnr.domain.game;

/** 핑/알람 전송 커맨드 */
public record SendPingCommand(
    String gameId,
    String senderId,
    PingType pingType,
    double latitude,
    double longitude
) {}
