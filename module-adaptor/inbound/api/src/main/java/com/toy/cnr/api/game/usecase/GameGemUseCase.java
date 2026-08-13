package com.toy.cnr.api.game.usecase;

import com.toy.cnr.api.game.response.GemsResponse;
import com.toy.cnr.application.game.service.GemService;
import com.toy.cnr.domain.common.CommandResult;
import org.springframework.stereotype.Component;

/**
 * 보석 조회 유즈케이스 (도둑 전용).
 */
@Component
public class GameGemUseCase {

    private final GemService gemService;

    public GameGemUseCase(GemService gemService) {
        this.gemService = gemService;
    }

    public CommandResult<GemsResponse> getAvailableGems(String gameId) {
        return gemService.getAvailableGems(gameId)
            .map(GemsResponse::from);
    }
}
