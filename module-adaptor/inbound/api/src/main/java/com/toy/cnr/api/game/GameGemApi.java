package com.toy.cnr.api.game;

import com.toy.cnr.api.common.util.ResponseMapper;
import com.toy.cnr.api.game.response.GemsResponse;
import com.toy.cnr.api.game.usecase.GameGemUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Game Gem", description = "인게임 보석 API (도둑 전용)")
@RestController
@RequestMapping("/v1/game")
public class GameGemApi {

    private final GameGemUseCase gameGemUseCase;

    public GameGemApi(GameGemUseCase gameGemUseCase) {
        this.gameGemUseCase = gameGemUseCase;
    }

    @Operation(
        summary = "보석 목록 조회",
        description = "현재 게임 맵에 스폰된 획득 가능한 보석 목록을 조회합니다. 도둑 전용."
    )
    @GetMapping("/{gameId}/gems")
    public ResponseEntity<GemsResponse> getGems(@PathVariable String gameId) {
        return ResponseMapper.toResponseEntity(gameGemUseCase.getAvailableGems(gameId));
    }
}
