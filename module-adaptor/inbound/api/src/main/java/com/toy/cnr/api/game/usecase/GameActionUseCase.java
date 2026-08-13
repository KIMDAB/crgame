package com.toy.cnr.api.game.usecase;

import com.toy.cnr.api.game.request.*;
import com.toy.cnr.application.game.service.GameActionService;
import com.toy.cnr.domain.common.CommandResult;
import org.springframework.stereotype.Component;

/**
 * 인게임 액션 유즈케이스 (체포, 구출, 보석 획득, 핑).
 */
@Component
public class GameActionUseCase {

    private final GameActionService gameActionService;

    public GameActionUseCase(GameActionService gameActionService) {
        this.gameActionService = gameActionService;
    }

    public CommandResult<Void> arrest(String gameId, ArrestRequest request, String copsId) {
        return gameActionService.arrest(request.toCommand(gameId, copsId));
    }

    public CommandResult<Void> rescue(String gameId, RescueRequest request, String rescuerId) {
        return gameActionService.rescue(request.toCommand(gameId, rescuerId));
    }

    public CommandResult<Void> collectGem(String gameId, String gemId, String robberId) {
        return gameActionService.collectGem(new com.toy.cnr.domain.game.CollectGemCommand(gameId, robberId, gemId));
    }

    public CommandResult<Void> sendPing(String gameId, SendPingRequest request, String senderId) {
        return gameActionService.sendPing(request.toCommand(gameId, senderId));
    }
}
