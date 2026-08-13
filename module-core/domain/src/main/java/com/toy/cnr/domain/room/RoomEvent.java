package com.toy.cnr.domain.room;

/**
 * 방(Room) 에서 발생하는 이벤트를 나타내는 sealed interface.
 * <p>
 * 게임 시작 전 대기방 단계의 이벤트를 처리합니다.
 */
public sealed interface RoomEvent
    permits RoomEvent.PlayerJoined,
            RoomEvent.PlayerLeft
{
    String roomId();
    long timestamp();

    /** 플레이어가 방에 참가 */
    record PlayerJoined(
        String roomId,
        String playerId,
        String playerName,
        long timestamp
    ) implements RoomEvent {}

    /** 플레이어가 방을 나감 */
    record PlayerLeft(
        String roomId,
        String playerId,
        long timestamp
    ) implements RoomEvent {}
}
