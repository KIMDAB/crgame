package com.toy.cnr.api.room;

import com.toy.cnr.api.room.response.RoomEventResponse;
import com.toy.cnr.api.room.usecase.RoomEventUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Tag(name = "Room Event", description = "방 이벤트 실시간 구독 API")
@RestController
@RequestMapping("/v1/rooms")
public class RoomEventApi {

    private final RoomEventUseCase roomEventUseCase;

    public RoomEventApi(RoomEventUseCase roomEventUseCase) {
        this.roomEventUseCase = roomEventUseCase;
    }

    @Operation(
        summary = "방 이벤트 구독 (SSE)",
        description = """
            방에서 발생하는 이벤트를 실시간으로 수신합니다.
            Server-Sent Events 스트림으로 응답되며, SSE `event:` 필드로 이벤트 타입을 구분합니다.

            | event 타입    | 설명                    | data 주요 필드               |
            |--------------|------------------------|------------------------------|
            | PLAYER_JOINED | 새로운 플레이어가 방 참가 | playerId, playerName         |
            | PLAYER_LEFT   | 플레이어가 방을 나감     | playerId                     |
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "SSE 스트림 연결 성공",
                content = @Content(
                    mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                    schema = @Schema(implementation = RoomEventResponse.class)
                )
            )
        }
    )
    @PostMapping(
        value = "/{roomId}/events/subscribe",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter subscribeRoomEvents(@PathVariable String roomId) {
        var emitter = new SseEmitter(0L);

        var subscriberId = roomEventUseCase.subscribeToRoomEvents(
            roomId,
            event -> {
                try {
                    emitter.send(
                        SseEmitter.event()
                            .name(event.type())
                            .data(event)
                    );
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
        );

        emitter.onCompletion(() -> roomEventUseCase.unsubscribe(subscriberId));
        emitter.onTimeout(() -> roomEventUseCase.unsubscribe(subscriberId));
        emitter.onError(e -> roomEventUseCase.unsubscribe(subscriberId));

        return emitter;
    }
}
