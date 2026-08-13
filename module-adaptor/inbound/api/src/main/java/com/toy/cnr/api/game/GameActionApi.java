package com.toy.cnr.api.game;

import com.toy.cnr.api.common.util.ResponseMapper;
import com.toy.cnr.api.common.util.UserPrincipalAdaptorUtil;
import com.toy.cnr.api.game.request.*;
import com.toy.cnr.api.game.usecase.GameActionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Game Action", description = "인게임 액션 API (체포, 구출, 보석 획득, 핑)")
@RestController
@RequestMapping("/v1/game")
public class GameActionApi {

    private final GameActionUseCase gameActionUseCase;

    public GameActionApi(GameActionUseCase gameActionUseCase) {
        this.gameActionUseCase = gameActionUseCase;
    }

    @Operation(summary = "도둑 체포", description = "경찰이 일정 범위 내의 도둑을 체포합니다.")
    @PostMapping("/{gameId}/arrest")
    public ResponseEntity<Void> arrest(
        @PathVariable String gameId,
        @RequestBody ArrestRequest request
    ) {
        var user = UserPrincipalAdaptorUtil.getUserInfo();
        return ResponseMapper.toNoContentResponse(
            gameActionUseCase.arrest(gameId, request, user.id().toString())
        );
    }

    @Operation(summary = "동료 구출", description = "도둑이 체포된 동료를 구출합니다.")
    @PostMapping("/{gameId}/rescue")
    public ResponseEntity<Void> rescue(
        @PathVariable String gameId,
        @RequestBody RescueRequest request
    ) {
        var user = UserPrincipalAdaptorUtil.getUserInfo();
        return ResponseMapper.toNoContentResponse(
            gameActionUseCase.rescue(gameId, request, user.id().toString())
        );
    }

    @Operation(summary = "보석 획득", description = "도둑이 보석을 획득합니다.")
    @PostMapping("/{gameId}/gems/{gemId}/collect")
    public ResponseEntity<Void> collectGem(
        @PathVariable String gameId,
        @PathVariable String gemId
    ) {
        var user = UserPrincipalAdaptorUtil.getUserInfo();
        return ResponseMapper.toNoContentResponse(
            gameActionUseCase.collectGem(gameId, gemId, user.id().toString())
        );
    }

    @Operation(summary = "핑/알람 전송", description = "같은 역할 팀원에게 위치 알람을 전송합니다.")
    @PostMapping("/{gameId}/ping")
    public ResponseEntity<Void> sendPing(
        @PathVariable String gameId,
        @RequestBody SendPingRequest request
    ) {
        var user = UserPrincipalAdaptorUtil.getUserInfo();
        return ResponseMapper.toNoContentResponse(
            gameActionUseCase.sendPing(gameId, request, user.id().toString())
        );
    }
}
