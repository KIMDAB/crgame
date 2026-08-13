package com.toy.cnr.api.game;

import com.toy.cnr.api.common.util.ResponseMapper;
import com.toy.cnr.api.common.util.UserPrincipalAdaptorUtil;
import com.toy.cnr.api.game.response.RobberNearbyResponse;
import com.toy.cnr.api.game.usecase.GameGeoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Game Geo", description = "위치 기반 조회 API (경찰 반경 내 도둑 등)")
@RestController
@RequestMapping("/v1/game")
public class GameGeoApi {

    private final GameGeoUseCase gameGeoUseCase;

    public GameGeoApi(GameGeoUseCase gameGeoUseCase) {
        this.gameGeoUseCase = gameGeoUseCase;
    }

    @Operation(
        summary = "경찰 반경 내 도둑 조회",
        description = "현재 사용자(경찰) 위치를 기준으로, 지정한 반경(미터) 안에 있는 ACTIVE 상태 도둑 목록을 조회합니다. "
            + "거리는 Redis GeoHash로 계산하며, 반경(radiusMeters)은 호출 시 임의로 설정합니다. 경찰만 호출 가능합니다."
    )
    @GetMapping("/{gameId}/geo/robbers-nearby")
    public ResponseEntity<List<RobberNearbyResponse>> getRobbersNearby(
        @PathVariable String gameId,
        @Parameter(description = "반경(미터). 0 초과, 최대 10000", required = true, example = "50")
        @RequestParam double radiusMeters
    ) {
        var user = UserPrincipalAdaptorUtil.getUserInfo();
        return ResponseMapper.toResponseEntity(
            gameGeoUseCase.getRobbersNearby(gameId, user.id().toString(), radiusMeters)
        );
    }
}
