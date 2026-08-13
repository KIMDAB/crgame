package com.toy.cnr.domain.room;

/**
 * 방 설정 정보.
 *
 * @param gameMode             게임 모드
 * @param minPlayers           최소 참가 인원
 * @param maxPlayers           최대 참가 인원
 * @param copsCount            경찰 수
 * @param robbersCount         도둑 수
 * @param gameDurationMinutes  게임 시간 (분)
 * @param escapeTimeMinutes    도둑 도망 시간 (분, 게임 시작 후 경찰이 움직이지 못하는 시간)
 * @param actionRadiusMeters   체포/구출/보석 획득 가능 거리 (미터, 기본값 1m)
 * @param mapZone              게임 장소(집결지·활동/감옥/제한 구역 좌표). PUT /v1/rooms/{id}/settings 로 저장
 */
public record RoomSettings(
    GameMode gameMode,
    int minPlayers,
    int maxPlayers,
    int copsCount,
    int robbersCount,
    int gameDurationMinutes,
    int escapeTimeMinutes,
    double actionRadiusMeters,
    MapZone mapZone
) {
    public static RoomSettings defaultSettings() {
        return new RoomSettings(
            GameMode.BASIC,
            2,
            10,
            1,
            1,
            10,
            1,
            1.0,
            null
        );
    }
}
