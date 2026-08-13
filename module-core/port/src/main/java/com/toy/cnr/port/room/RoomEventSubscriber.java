package com.toy.cnr.port.room;

import com.toy.cnr.port.room.model.RoomEventDto;

import java.util.function.Consumer;

/**
 * 방 이벤트 구독 포트.
 * <p>
 * 동일 방에 복수의 클라이언트(SSE 커넥션)가 독립적으로 구독할 수 있도록
 * 구독 단위를 식별하는 {@code subscriberId}를 반환합니다.
 */
public interface RoomEventSubscriber {

    /**
     * 방 이벤트 채널을 구독합니다.
     *
     * @param roomId    방 ID
     * @param onMessage 이벤트 수신 시 호출되는 콜백
     * @return 구독 해제에 사용할 subscriberId
     */
    String subscribe(String roomId, Consumer<RoomEventDto> onMessage);

    /**
     * 구독을 해제합니다.
     *
     * @param subscriberId {@link #subscribe}가 반환한 ID
     */
    void unsubscribe(String subscriberId);
}
