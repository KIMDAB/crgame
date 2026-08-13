package com.toy.cnr.api.game.request;

import com.toy.cnr.domain.game.RescueCommand;

public record RescueRequest(String rescuedId) {

    public RescueCommand toCommand(String gameId, String rescuerId) {
        return new RescueCommand(gameId, rescuerId, rescuedId);
    }
}
