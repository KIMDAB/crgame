package com.toy.cnr.domain.userconsent;

import java.time.LocalDateTime;

public record UserConsentUpdateCommand(
    String consentItem,
    Boolean agreed,
    LocalDateTime lastModifiedDate,
    String deviceId,
    Long userId
) {
}
