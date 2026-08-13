package com.toy.cnr.port.userconsent.model;

public record UserConsentCreateDto(
    String consentItem,
    Boolean agreed,
    String deviceId,
    Long userId
) {
}
