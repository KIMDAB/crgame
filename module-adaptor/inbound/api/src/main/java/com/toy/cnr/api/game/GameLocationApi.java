package com.toy.cnr.api.game;

import com.toy.cnr.api.common.util.ResponseMapper;
import com.toy.cnr.api.common.util.UserPrincipalAdaptorUtil;
import com.toy.cnr.api.game.request.LocationPublishRequest;
import com.toy.cnr.api.game.request.LocationSubscribeRequest;
import com.toy.cnr.api.game.response.LocationResponse;
import com.toy.cnr.api.game.usecase.GameLocationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Tag(name = "Game Location", description = "실시간 GPS 좌표 브로드캐스트 API")
@RestController
@RequestMapping("/v1/game")
public class GameLocationApi {

    private final GameLocationUseCase gameLocationUseCase;

    public GameLocationApi(GameLocationUseCase gameLocationUseCase) {
        this.gameLocationUseCase = gameLocationUseCase;
    }

    @Operation(
        summary = "내 현재 위치 조회",
        description = "Redis에 저장된 현재 사용자의 최신 좌표를 조회합니다. 위치를 한 번도 발행하지 않았으면 404입니다."
    )
    @GetMapping("/{gameId}/location/me")
    public ResponseEntity<LocationResponse> getMyLocation(@PathVariable String gameId) {
        var user = UserPrincipalAdaptorUtil.getUserInfo();
        return ResponseMapper.toResponseEntity(
            gameLocationUseCase.getMyLocation(gameId, user.id().toString())
        );
    }

    @Operation(
        summary = "특정 유저 위치 조회",
        description = "해당 게임에 참여한 특정 플레이어의 Redis 저장 최신 좌표를 조회합니다. "
            + "위치를 한 번도 발행하지 않은 플레이어면 404입니다. 인증된 사용자만 호출 가능합니다."
    )
    @GetMapping("/{gameId}/location/{playerId}")
    public ResponseEntity<LocationResponse> getPlayerLocation(
        @PathVariable String gameId,
        @PathVariable String playerId
    ) {
        return ResponseMapper.toResponseEntity(
            gameLocationUseCase.getPlayerLocation(gameId, playerId)
        );
    }

    @Operation(
        summary = "좌표 발행",
        description = """
            플레이어 좌표를 Redis GeoHash에 저장하고, Pub/Sub으로 해당 플레이어를 구독 중인 클라이언트에게 브로드캐스트합니다.
            ```
            curl -X POST '{host}/v1/game/location' \\
              -H 'Authorization: Bearer <ACCESS_TOKEN>' \\
              -H 'Content-Type: application/json' \\
              -d '{"gameId":"game-1","playerId":"player-1","longitude":127.0276,"latitude":37.4979}'
            ```
            """
    )
    @PostMapping("/location")
    public ResponseEntity<LocationResponse> publishLocation(
        @RequestBody LocationPublishRequest request
    ) {
        return ResponseMapper.toResponseEntity(
            gameLocationUseCase.publishLocation(request)
        );
    }

    @Operation(
        summary = "좌표 구독 (SSE)",
        description = """
            지정한 플레이어들의 좌표 변경을 **Server-Sent Events** 스트림으로 실시간 수신합니다.
            연결을 유지하는 동안 `event: location` 이벤트가 지속적으로 수신되며, 연결을 끊으면 자동으로 구독이 해제됩니다.

            > ⚠️ Swagger UI에서는 SSE 응답을 직접 확인하기 어렵습니다. 아래 cURL 명령으로 테스트하세요.

            ```
            curl -X POST '{host}/v1/game/location/subscribe' \\
              -H 'Authorization: Bearer <ACCESS_TOKEN>' \\
              -H 'Content-Type: application/json' \\
              -H 'Accept: text/event-stream' \\
              --no-buffer \\
              -d '{"gameId":"game-1","playerIds":["player-1","player-2"]}'
            ```
            수신 예시:
            ```
            event: location
            data: {"playerId":"player-1","longitude":127.0276,"latitude":37.4979,"timestamp":1741996800000}
            ```
            """
    )
    @PostMapping(
        value = "/location/subscribe",
        produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter subscribeLocation(@RequestBody LocationSubscribeRequest request) {
        var emitter = new SseEmitter(0L);

        gameLocationUseCase.subscribeToPlayers(
            request.gameId(),
            request.playerIds(),
            location -> {
                try {
                    emitter.send(
                        SseEmitter.event()
                            .name("location")
                            .data(location)
                    );
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
        );

        emitter.onCompletion(() ->
            gameLocationUseCase.unsubscribe(request.gameId(), request.playerIds())
        );
        emitter.onTimeout(() ->
            gameLocationUseCase.unsubscribe(request.gameId(), request.playerIds())
        );
        emitter.onError(e ->
            gameLocationUseCase.unsubscribe(request.gameId(), request.playerIds())
        );

        return emitter;
    }
}
