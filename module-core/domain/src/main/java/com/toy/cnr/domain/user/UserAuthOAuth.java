package com.toy.cnr.domain.user;

public record UserAuthOAuth(
    Long userId,
    String provider,
    String oauthId,
    String accessToken
) {
}
