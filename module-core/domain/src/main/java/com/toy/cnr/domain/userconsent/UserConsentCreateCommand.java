package com.toy.cnr.domain.userconsent;

public record UserConsentCreateCommand(
    ConsentItem consentItem,
    Boolean agreed,
    String deviceId,
    Long userId
) {
}
