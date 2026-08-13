package com.toy.cnr.port.userconsent.model;

import java.time.LocalDateTime;

public record UserConsentUpdateDto(
    String consentItem,
    Boolean agreed,
    LocalDateTime lastModifiedDate,
    String deviceId,
    Long userId
) {
}
