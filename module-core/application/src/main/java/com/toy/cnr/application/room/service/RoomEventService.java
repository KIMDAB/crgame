package com.toy.cnr.application.room.service;

import com.toy.cnr.application.room.mapper.RoomEventMapper;
import com.toy.cnr.domain.room.RoomEvent;
import com.toy.cnr.port.room.RoomEventPublisher;
import com.toy.cnr.port.room.RoomEventSubscriber;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * 방 이벤트 발행/구독 서비스.
 * <p>
 * RoomEventPublisher(Pub/Sub PUBLISH),
 * RoomEventSubscriber(Pub/Sub SUBSCRIBE) 포트를 사용합니다.
 */
@Service
public class RoomEventService {

    private final RoomEventPublisher roomEventPublisher;
    private final RoomEventSubscriber roomEventSubscriber;

    public RoomEventService(
        RoomEventPublisher roomEventPublisher,
        RoomEventSubscriber roomEventSubscriber
    ) {
        this.roomEventPublisher = roomEventPublisher;
        this.roomEventSubscriber = roomEventSubscriber;
    }

    /**
     * 방 이벤트를 Pub/Sub으로 발행합니다.
     */
    public void publish(RoomEvent event) {
        roomEventPublisher.publish(event.roomId(), RoomEventMapper.toDto(event));
    }

    /**
     * 방 이벤트 채널을 구독합니다.
     *
     * @param roomId  방 ID
     * @param onEvent 이벤트 수신 시 호출되는 콜백 (도메인 모델 전달)
     * @return 구독 해제에 사용할 subscriberId
     */
    public String subscribe(String roomId, Consumer<RoomEvent> onEvent) {
        return roomEventSubscriber.subscribe(roomId, dto ->
            onEvent.accept(RoomEventMapper.toDomain(dto))
        );
    }

    /**
     * 구독을 해제합니다.
     */
    public void unsubscribe(String subscriberId) {
        roomEventSubscriber.unsubscribe(subscriberId);
    }
}
