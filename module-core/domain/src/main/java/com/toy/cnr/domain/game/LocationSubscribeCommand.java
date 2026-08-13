package com.toy.cnr.domain.game;

import java.util.List;

/**
 * 좌표 구독 커맨드.
 *
 * @param gameId    게임 세션 ID
 * @param playerIds 구독할 플레이어 ID 목록
 */
public record LocationSubscribeCommand(
    String gameId,
    List<String> playerIds
) {
}
