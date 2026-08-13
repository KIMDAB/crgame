package com.toy.cnr.domain.userconsent;

import java.time.LocalDateTime;

public record UserConsent(
    Long id,
    ConsentItem consentItem,
    Boolean agreed,
    LocalDateTime lastModifiedDate,
    String deviceId,
    Long userId
) {
}
