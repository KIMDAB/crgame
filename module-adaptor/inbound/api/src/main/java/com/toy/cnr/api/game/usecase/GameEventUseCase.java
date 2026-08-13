package com.toy.cnr.api.game.usecase;

import com.toy.cnr.api.game.response.GameEventResponse;
import com.toy.cnr.application.game.service.GameEventService;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * 게임 이벤트 구독 유즈케이스.
 * <p>
 * 게임 이벤트 SSE 구독을 오케스트레이션합니다.
 * HTTP 관심사({@code SseEmitter}, {@code ResponseEntity})는 Controller가 담당합니다.
 */
@Component
public class GameEventUseCase {

    private final GameEventService gameEventService;

    public GameEventUseCase(GameEventService gameEventService) {
        this.gameEventService = gameEventService;
    }

    /**
     * 게임 이벤트 채널을 구독합니다.
     *
     * @param gameId  게임 세션 ID
     * @param onEvent 이벤트 수신 시 호출되는 콜백
     * @return 구독 해제에 사용할 subscriberId
     */
    public String subscribeToGameEvents(String gameId, Consumer<GameEventResponse> onEvent) {
        return gameEventService.subscribe(gameId, event ->
            onEvent.accept(GameEventResponse.from(event))
        );
    }

    /**
     * 구독을 해제합니다.
     */
    public void unsubscribe(String subscriberId) {
        gameEventService.unsubscribe(subscriberId);
    }
}
