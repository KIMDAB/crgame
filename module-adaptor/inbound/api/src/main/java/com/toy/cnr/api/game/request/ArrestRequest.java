package com.toy.cnr.api.game.request;

import com.toy.cnr.domain.game.ArrestCommand;

public record ArrestRequest(String robberId) {

    public ArrestCommand toCommand(String gameId, String copsId) {
        return new ArrestCommand(gameId, copsId, robberId);
    }
}
