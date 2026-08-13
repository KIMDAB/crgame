package com.toy.cnr.api.game;

import com.toy.cnr.api.game.request.GameEventSubscribeRequest;
import com.toy.cnr.api.game.response.GameEventResponse;
import com.toy.cnr.api.game.usecase.GameEventUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Tag(name = "Game Event", description = "게임 이벤트 실시간 구독 API")
@RestController
@RequestMapping("/v1/game")
public class GameEventApi {

    private final GameEventUseCase gameEventUseCase;

    public GameEventApi(GameEventUseCase gameEventUseCase) {
        this.gameEventUseCase = gameEventUseCase;
    }

    @Operation(
        summary = "게임 이벤트 구독 (SSE)",
        description = """
            게임에서 발생하는 이벤트를 실시간으로 수신합니다.
            Server-Sent Events 스트림으로 응답되며, SSE `event:` 필드로 이벤트 타입을 구분합니다.

            | event 타입              | 설명                          | data 주요 필드                        |
            |------------------------|-------------------------------|--------------------------------------|
            | PLAYER_ARRESTED        | 경찰이 도둑을 체포             | copsId, robberId                     |
            | PLAYER_RESCUED         | 도둑이 체포된 동료를 구출      | rescuerId, rescuedId                 |
            | PRISON_ESCAPE_WARNING  | 체포된 도둑이 감옥 범위 이탈   | playerId                             |
            | ANNOUNCEMENT           | 방장 공지                     | senderId, message                    |
            | GAME_STARTED           | 게임 시작                     | -                                    |
            | GAME_ENDED             | 게임 종료                     | winnerRole                           |
            | ROLE_ASSIGNED          | 인게임 역할 배정               | playerId, role (POLICE / THIEF)      |
            """,
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "SSE 스트림 연결 성공",
                content = @Content(
                    mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                    schema = @Schema(implementation = GameEventResponse.class)
                )
            )
        }
    )
    @PostMapping(
        value = "/events/subscribe",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter subscribeEvents(@RequestBody GameEventSubscribeRequest request) {
        var emitter = new SseEmitter(0L);

        var subscriberId = gameEventUseCase.subscribeToGameEvents(
            request.gameId(),
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

        emitter.onCompletion(() -> gameEventUseCase.unsubscribe(subscriberId));
        emitter.onTimeout(() -> gameEventUseCase.unsubscribe(subscriberId));
        emitter.onError(e -> gameEventUseCase.unsubscribe(subscriberId));

        return emitter;
    }
}
