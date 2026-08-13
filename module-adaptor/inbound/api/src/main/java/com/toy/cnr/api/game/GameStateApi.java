package com.toy.cnr.api.game;

import com.toy.cnr.api.common.util.ResponseMapper;
import com.toy.cnr.api.game.response.*;
import com.toy.cnr.api.game.usecase.GameStateUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Game State", description = "인게임 상태 조회 API")
@RestController
@RequestMapping("/v1/game")
public class GameStateApi {

    private final GameStateUseCase gameStateUseCase;

    public GameStateApi(GameStateUseCase gameStateUseCase) {
        this.gameStateUseCase = gameStateUseCase;
    }

    @Operation(summary = "게임 상태 조회", description = "현재 게임의 상태(진행 단계, 시작/종료 시각)를 조회합니다.")
    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateResponse> getGameState(@PathVariable String gameId) {
        return ResponseMapper.toResponseEntity(gameStateUseCase.getGameState(gameId));
    }

    @Operation(summary = "인게임 플레이어 목록", description = "게임에 참가한 전체 플레이어 목록과 현재 상태를 조회합니다.")
    @GetMapping("/{gameId}/players")
    public ResponseEntity<InGamePlayersResponse> getPlayers(@PathVariable String gameId) {
        return ResponseMapper.toResponseEntity(gameStateUseCase.getPlayers(gameId));
    }

    @Operation(summary = "개인 인게임 정보", description = "특정 플레이어의 역할, 상태, 성과를 조회합니다.")
    @GetMapping("/{gameId}/players/{playerId}")
    public ResponseEntity<InGamePlayerResponse> getPlayer(
        @PathVariable String gameId,
        @PathVariable String playerId
    ) {
        return ResponseMapper.toResponseEntity(gameStateUseCase.getPlayer(gameId, playerId));
    }

    @Operation(summary = "게임 결과 조회", description = "종료된 게임의 최종 결과를 조회합니다.")
    @GetMapping("/{gameId}/result")
    public ResponseEntity<GameResultResponse> getResult(@PathVariable String gameId) {
        return ResponseMapper.toResponseEntity(gameStateUseCase.getResult(gameId));
    }
}
