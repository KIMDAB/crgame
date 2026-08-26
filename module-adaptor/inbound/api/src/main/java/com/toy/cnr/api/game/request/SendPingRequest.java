package com.toy.cnr.api.game.request;

import com.toy.cnr.domain.game.PingType;
import com.toy.cnr.domain.game.SendPingCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "핑/알람 전송 요청")
public record SendPingRequest(
    @Schema(
        description = """
            핑 타입. THIEF_* 는 도둑 팀원에게만, POLICE_* 는 경찰 팀원에게만 전달됩니다.
            발신자의 역할과 타입이 불일치하면 요청이 거부됩니다.

            | 값                      | 역할   | 의미                  |
            |-------------------------|--------|-----------------------|
            | THIEF_COPS_SPOTTED      | THIEF  | 경찰 목격             |
            | THIEF_DANGER            | THIEF  | 위험                  |
            | THIEF_GEM_FOUND         | THIEF  | 보석 발견             |
            | THIEF_GATHER            | THIEF  | 집결                  |
            | THIEF_RUN               | THIEF  | 도망                  |
            | POLICE_ROBBER_SPOTTED   | POLICE | 도둑 목격             |
            | POLICE_GEM_FOUND        | POLICE | 보석 위치 공유        |
            | POLICE_SUPPORT_NEEDED   | POLICE | 지원 요청             |
            | POLICE_ON_MY_WAY        | POLICE | 이동 중               |
            | POLICE_GATHER           | POLICE | 집결                  |
            """,
        example = "THIEF_DANGER",
        allowableValues = {
            "THIEF_COPS_SPOTTED", "THIEF_DANGER", "THIEF_GEM_FOUND", "THIEF_GATHER", "THIEF_RUN",
            "POLICE_ROBBER_SPOTTED", "POLICE_GEM_FOUND", "POLICE_SUPPORT_NEEDED", "POLICE_ON_MY_WAY", "POLICE_GATHER"
        }
    )
    String pingType,

    @Schema(description = "핑 위도", example = "37.5665")
    double latitude,

    @Schema(description = "핑 경도", example = "126.9780")
    double longitude
) {
    public SendPingCommand toCommand(String gameId, String senderId) {
        return new SendPingCommand(gameId, senderId, PingType.valueOf(pingType), latitude, longitude);
    }
}
