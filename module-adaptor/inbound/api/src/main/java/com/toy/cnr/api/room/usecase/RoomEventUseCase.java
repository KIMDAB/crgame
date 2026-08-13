package com.toy.cnr.api.room.usecase;

import com.toy.cnr.api.room.response.RoomEventResponse;
import com.toy.cnr.application.room.service.RoomEventService;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * 방 이벤트 구독 유즈케이스.
 * <p>
 * 방 이벤트 SSE 구독을 오케스트레이션합니다.
 * HTTP 관심사({@code SseEmitter}, {@code ResponseEntity})는 Controller가 담당합니다.
 */
@Component
public class RoomEventUseCase {

    private final RoomEventService roomEventService;

    public RoomEventUseCase(RoomEventService roomEventService) {
        this.roomEventService = roomEventService;
    }

    /**
     * 방 이벤트 채널을 구독합니다.
     *
     * @param roomId  방 ID
     * @param onEvent 이벤트 수신 시 호출되는 콜백
     * @return 구독 해제에 사용할 subscriberId
     */
    public String subscribeToRoomEvents(String roomId, Consumer<RoomEventResponse> onEvent) {
        return roomEventService.subscribe(roomId, event ->
            onEvent.accept(RoomEventResponse.from(event))
        );
    }

    /**
     * 구독을 해제합니다.
     */
    public void unsubscribe(String subscriberId) {
        roomEventService.unsubscribe(subscriberId);
    }
}
