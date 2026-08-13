package com.toy.cnr.port.game.model;

import java.util.Map;

/**
 * 게임 이벤트 데이터 전달 객체.
 * <p>
 * Redis Pub/Sub 직렬화/역직렬화에 사용됩니다.
 * {@code data} 필드는 이벤트 타입별 페이로드를 담습니다.
 */
public record GameEventDto(
    String gameId,
    String type,
    Map<String, String> data,
    long timestamp
) {}
