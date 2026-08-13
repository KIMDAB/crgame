package com.toy.cnr.domain.room;

/** 게임 시작 커맨드 (방장만 가능) */
public record GameStartCommand(
    String roomId,
    String requesterId
) {}
