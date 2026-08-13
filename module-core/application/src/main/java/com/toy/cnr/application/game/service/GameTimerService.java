package com.toy.cnr.application.game.service;

import com.toy.cnr.domain.game.GameEvent;
import com.toy.cnr.domain.game.GameStatus;
import com.toy.cnr.domain.game.PlayerRole;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.GameRegistryStore;
import com.toy.cnr.port.game.GameStateStore;
import com.toy.cnr.port.game.GameTimerPort;
import com.toy.cnr.port.game.model.GameStateDto;
import org.springframework.stereotype.Service;

/**
 * 게임 라이프사이클 타이머 서비스.
 * <p>
 * 게임 시작 시 두 개의 타이머를 등록합니다:
 * <ol>
 *   <li>escape timer: escapeTimeMinutes 후 ESCAPE_PHASE → PLAYING 전환</li>
 *   <li>end timer: gameDurationMinutes 후 게임이 아직 PLAYING이면 ENDED + GameEnded(THIEF) 발행</li>
 * </ol>
 */
@Service
public class GameTimerService {

    private final GameTimerPort gameTimerPort;
    private final GameStateStore gameStateStore;
    private final GameRegistryStore gameRegistryStore;
    private final GameEventService gameEventService;

    public GameTimerService(
        GameTimerPort gameTimerPort,
        GameStateStore gameStateStore,
        GameRegistryStore gameRegistryStore,
        GameEventService gameEventService
    ) {
        this.gameTimerPort = gameTimerPort;
        this.gameStateStore = gameStateStore;
        this.gameRegistryStore = gameRegistryStore;
        this.gameEventService = gameEventService;
    }

    /**
     * 게임 시작 시 라이프사이클 타이머를 등록합니다.
     *
     * @param gameId   게임 ID
     * @param escapeMs 도둑 도망 시간 (밀리초) — 이후 PLAYING으로 전환
     * @param totalMs  전체 게임 시간 (밀리초) — 이후 도둑 승리로 종료
     */
    public void scheduleGame(String gameId, long escapeMs, long totalMs) {
        // Timer 1: ESCAPE_PHASE → PLAYING
        gameTimerPort.schedule("escape:" + gameId, escapeMs, () -> {
            var result = gameStateStore.getGameState(gameId);
            if (result instanceof RepositoryResult.Found<GameStateDto> found
                && found.data().status().equals(GameStatus.ESCAPE_PHASE.name())) {
                gameStateStore.updateStatus(gameId, GameStatus.PLAYING.name());
            }
        });

        // Timer 2: PLAYING → ENDED (robbers win by time)
        gameTimerPort.schedule("end:" + gameId, totalMs, () -> {
            var result = gameStateStore.getGameState(gameId);
            if (result instanceof RepositoryResult.Found<GameStateDto> found
                && found.data().status().equals(GameStatus.PLAYING.name())) {
                endGame(gameId, PlayerRole.THIEF.name());
            }
        });
    }

    /**
     * 게임을 즉시 종료합니다 (경찰 승리 시 호출).
     * 등록된 타이머를 취소하고 ENDED 상태로 전환 후 GameEnded 이벤트를 발행합니다.
     *
     * @param gameId     게임 ID
     * @param winnerRole 승리 역할 이름 ("POLICE" 또는 "THIEF")
     */
    public void endGame(String gameId, String winnerRole) {
        cancelTimers(gameId);
        gameStateStore.updateStatus(gameId, GameStatus.ENDED.name());
        gameRegistryStore.unregister(gameId);
        gameEventService.publish(new GameEvent.GameEnded(
            gameId, winnerRole, System.currentTimeMillis()
        ));
    }

    private void cancelTimers(String gameId) {
        gameTimerPort.cancel("escape:" + gameId);
        gameTimerPort.cancel("end:" + gameId);
    }
}
