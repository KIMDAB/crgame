package com.toy.cnr.domain.game;

/** 인게임 알람(핑) 타입 — 각 값은 해당 role 팀원에게만 전달된다. */
public enum PingType {
    THIEF_COPS_SPOTTED(PlayerRole.THIEF),
    THIEF_DANGER(PlayerRole.THIEF),
    THIEF_GEM_FOUND(PlayerRole.THIEF),
    THIEF_GATHER(PlayerRole.THIEF),
    THIEF_RUN(PlayerRole.THIEF),

    POLICE_ROBBER_SPOTTED(PlayerRole.POLICE),
    POLICE_GEM_FOUND(PlayerRole.POLICE),
    POLICE_SUPPORT_NEEDED(PlayerRole.POLICE),
    POLICE_ON_MY_WAY(PlayerRole.POLICE),
    POLICE_GATHER(PlayerRole.POLICE);

    private final PlayerRole role;

    PingType(PlayerRole role) {
        this.role = role;
    }

    public PlayerRole role() {
        return role;
    }
}
