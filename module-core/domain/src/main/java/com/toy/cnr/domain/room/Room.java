package com.toy.cnr.domain.room;

import java.util.List;

/**
 * 게임 방(Room) 도메인 모델.
 *
 * @param roomId    방 ID (4글자 영문대문자+숫자, 예: "ABCD")
 * @param hostId    방장 플레이어 ID
 * @param settings  방 설정
 * @param status    방 상태
 * @param players   참가자 목록
 * @param createdAt 방 생성 시각 (epoch millis)
 */
public record Room(
    String roomId,
    String hostId,
    RoomSettings settings,
    RoomStatus status,
    List<RoomPlayer> players,
    long createdAt
) {}
