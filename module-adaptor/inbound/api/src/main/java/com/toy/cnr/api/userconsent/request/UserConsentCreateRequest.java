package com.toy.cnr.api.userconsent.request;

import com.toy.cnr.domain.userconsent.ConsentItem;
import com.toy.cnr.domain.userconsent.UserConsentCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "동의값 저장 요청")
public record UserConsentCreateRequest(
    @Schema(description = "디바이스 아이디", requiredMode = Schema.RequiredMode.REQUIRED)
    String deviceId,

    @Schema(description = "유저 아이디 (로그인 시에만 전달)")
    Long userId,

    @Schema(description = "동의 항목 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    List<ConsentEntry> consents
) {
    @Schema(description = "동의 항목")
    public record ConsentEntry(
        @Schema(description = "동의 항목 코드")
        ConsentItem consentItem,

        @Schema(description = "동의 여부")
        Boolean agreed
    ) {}

    public List<UserConsentCreateCommand> toCommands() {
        return consents.stream()
            .map(c -> new UserConsentCreateCommand(c.consentItem(), c.agreed(), deviceId, userId))
            .toList();
    }
}
