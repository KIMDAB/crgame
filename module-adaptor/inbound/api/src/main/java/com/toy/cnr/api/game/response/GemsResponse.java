package com.toy.cnr.api.game.response;

import com.toy.cnr.domain.game.Gem;

import java.util.List;

public record GemsResponse(List<GemResponse> gems) {

    public static GemsResponse from(List<Gem> gems) {
        return new GemsResponse(gems.stream().map(GemResponse::from).toList());
    }
}
