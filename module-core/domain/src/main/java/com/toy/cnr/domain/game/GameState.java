package com.toy.cnr.domain.game;

import com.toy.cnr.domain.room.RoomSettings;

/**
 * 인게임 게임 상태.
 *
 * @param gameId    게임 ID (UUID)
 * @param roomId    방 ID
 * @param status    게임 진행 상태
 * @param settings  게임 설정 (방 설정에서 복사)
 * @param startedAt 게임 시작 시각 (epoch millis)
 * @param endsAt    게임 종료 예정 시각 (epoch millis)
 */
public record GameState(
    String gameId,
    String roomId,
    GameStatus status,
    RoomSettings settings,
    long startedAt,
    long endsAt
) {}
