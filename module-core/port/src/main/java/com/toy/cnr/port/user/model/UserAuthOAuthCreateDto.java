package com.toy.cnr.port.user.model;

public record UserAuthOAuthCreateDto(Long userId, String provider, String oauthId, String accessToken) {}
