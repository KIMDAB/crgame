package com.toy.cnr.api.userconsent.response;

import com.toy.cnr.domain.userconsent.ConsentItem;
import com.toy.cnr.domain.userconsent.UserConsent;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "동의값 응답")
public record UserConsentResponse(
    @Schema(description = "ID")
    Long id,

    @Schema(description = "동의 항목")
    ConsentItem consentItem,

    @Schema(description = "동의 여부")
    Boolean agreed,

    @Schema(description = "최종 변경 날짜")
    LocalDateTime lastModifiedDate,

    @Schema(description = "디바이스 아이디")
    String deviceId,

    @Schema(description = "유저 아이디")
    Long userId
) {
    public static UserConsentResponse from(UserConsent userConsent) {
        return new UserConsentResponse(
            userConsent.id(),
            userConsent.consentItem(),
            userConsent.agreed(),
            userConsent.lastModifiedDate(),
            userConsent.deviceId(),
            userConsent.userId()
        );
    }
}
