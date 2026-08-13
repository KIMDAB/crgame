package com.toy.cnr.api.game.request;

import com.toy.cnr.domain.game.PingType;
import com.toy.cnr.domain.game.SendPingCommand;

public record SendPingRequest(
    String pingType,
    double latitude,
    double longitude
) {
    public SendPingCommand toCommand(String gameId, String senderId) {
        return new SendPingCommand(gameId, senderId, PingType.valueOf(pingType), latitude, longitude);
    }
}
