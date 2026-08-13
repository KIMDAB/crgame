package com.toy.cnr.api.userconsent.request;

import com.toy.cnr.domain.userconsent.UserConsentUpdateCommand;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "UserConsent 수정 요청")
public record UserConsentUpdateRequest(
    @Schema(description = "consentItem")
    String consentItem,

    @Schema(description = "agreed")
    Boolean agreed,

    @Schema(description = "lastModifiedDate")
    LocalDateTime lastModifiedDate,

    @Schema(description = "deviceId")
    String deviceId,

    @Schema(description = "userId")
    Long userId
) {
    public UserConsentUpdateCommand toCommand() {
        return new UserConsentUpdateCommand(this.consentItem, this.agreed, this.lastModifiedDate, this.deviceId, this.userId);
    }
}
