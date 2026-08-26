package com.toy.cnr.application.game.service;

import com.toy.cnr.application.game.mapper.GameEventMapper;
import com.toy.cnr.domain.game.GameEvent;
import com.toy.cnr.domain.game.PingType;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.game.GameEventPublisher;
import com.toy.cnr.port.game.GameEventSubscriber;
import com.toy.cnr.port.game.InGamePlayerStore;
import com.toy.cnr.port.game.model.InGamePlayerDto;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * 게임 이벤트 발행/구독 서비스.
 * <p>
 * GameEventPublisher(Pub/Sub PUBLISH),
 * GameEventSubscriber(Pub/Sub SUBSCRIBE) 포트를 사용합니다.
 */
@Service
public class GameEventService {

    private final GameEventPublisher gameEventPublisher;
    private final GameEventSubscriber gameEventSubscriber;
    private final InGamePlayerStore inGamePlayerStore;

    public GameEventService(
        GameEventPublisher gameEventPublisher,
        GameEventSubscriber gameEventSubscriber,
        InGamePlayerStore inGamePlayerStore
    ) {
        this.gameEventPublisher = gameEventPublisher;
        this.gameEventSubscriber = gameEventSubscriber;
        this.inGamePlayerStore = inGamePlayerStore;
    }

    /**
     * 게임 이벤트를 Pub/Sub으로 발행합니다.
     */
    public void publish(GameEvent event) {
        gameEventPublisher.publish(event.gameId(), GameEventMapper.toDto(event));
    }

    /**
     * 게임 이벤트 채널을 구독합니다.
     *
     * <ul>
     *   <li>ROLE_ASSIGNED — 본인 playerId 일치 시에만 전달</li>
     *   <li>PRISON_ESCAPE_WARNING — POLICE 역할에게만 전달</li>
     *   <li>PING_ALERT — 같은 역할(팀)에게만 전달</li>
     * </ul>
     *
     * @param gameId   게임 세션 ID
     * @param playerId 구독하는 플레이어 ID
     * @param onEvent  이벤트 수신 시 호출되는 콜백 (도메인 모델 전달)
     * @return 구독 해제에 사용할 subscriberId
     */
    public String subscribe(String gameId, String playerId, Consumer<GameEvent> onEvent) {
        var playerRole = resolvePlayerRole(gameId, playerId);

        return gameEventSubscriber.subscribe(gameId, dto -> {
            switch (dto.type()) {
                case "ROLE_ASSIGNED" -> {
                    if (!playerId.equals(dto.data().get("playerId"))) return;
                }
                case "PRISON_ESCAPE_WARNING" -> {
                    if (!"POLICE".equals(playerRole)) return;
                }
                case "PING_ALERT" -> {
                    if (playerRole == null) return;
                    var pingTypeStr = dto.data().get("pingType");
                    if (pingTypeStr != null) {
                        try {
                            if (!PingType.valueOf(pingTypeStr).role().name().equals(playerRole)) return;
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
                default -> { /* 나머지 이벤트는 전체 수신 */ }
            }
            onEvent.accept(GameEventMapper.toDomain(dto));
        });
    }

    /**
     * 구독을 해제합니다.
     */
    public void unsubscribe(String subscriberId) {
        gameEventSubscriber.unsubscribe(subscriberId);
    }

    private String resolvePlayerRole(String gameId, String playerId) {
        var result = inGamePlayerStore.getPlayer(gameId, playerId);
        if (result instanceof RepositoryResult.Found<InGamePlayerDto> found) {
            return found.data().role();
        }
        return null;
    }
}
