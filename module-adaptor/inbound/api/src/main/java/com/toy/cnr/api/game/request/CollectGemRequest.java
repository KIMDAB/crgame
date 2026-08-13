package com.toy.cnr.api.game.request;

import com.toy.cnr.domain.game.CollectGemCommand;

public record CollectGemRequest() {

    public CollectGemCommand toCommand(String gameId, String robberId, String gemId) {
        return new CollectGemCommand(gameId, robberId, gemId);
    }
}
