package com.toy.cnr.port.room.model;

import java.util.Map;

/**
 * 방 이벤트 데이터 전달 객체.
 * <p>
 * Redis Pub/Sub 직렬화/역직렬화에 사용됩니다.
 */
public record RoomEventDto(
    String roomId,
    String type,
    Map<String, String> data,
    long timestamp
) {}
