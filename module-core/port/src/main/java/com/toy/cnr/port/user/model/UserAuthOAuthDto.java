package com.toy.cnr.port.user.model;

public record UserAuthOAuthDto(Long userId, String provider, String oauthId, String accessToken) {}
