package com.toy.cnr.port.room;

import com.toy.cnr.port.room.model.RoomEventDto;

/**
 * 방 이벤트 발행 포트.
 * <p>
 * 구현체는 Redis Pub/Sub 등 메시징 인프라를 사용합니다.
 */
public interface RoomEventPublisher {

    /**
     * 방 이벤트를 발행합니다.
     *
     * @param roomId 방 ID
     * @param event  발행할 이벤트 DTO
     */
    void publish(String roomId, RoomEventDto event);
}
