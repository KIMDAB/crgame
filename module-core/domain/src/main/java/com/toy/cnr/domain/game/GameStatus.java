package com.toy.cnr.domain.game;

public enum GameStatus {
    /** 게임 준비 단계 (역할 배정 직후) */
    PREPARING,
    /** 도둑 도망 단계 (경찰이 움직이지 못함) */
    ESCAPE_PHASE,
    /** 게임 진행 중 */
    PLAYING,
    /** 게임 종료 */
    ENDED
}
