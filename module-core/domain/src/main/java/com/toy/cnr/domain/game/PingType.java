package com.toy.cnr.domain.game;

/** 인게임 알람(핑) 타입 */
public enum PingType {
    // 도둑 전용
    ROBBERS_COPS_SPOTTED,
    ROBBERS_DANGER,
    ROBBERS_GEM_FOUND,
    ROBBERS_GATHER,
    ROBBERS_RUN,

    // 경찰 전용
    COPS_ROBBER_SPOTTED,
    COPS_GEM_FOUND,
    COPS_SUPPORT_NEEDED,
    COPS_ON_MY_WAY,
    COPS_GATHER
}
