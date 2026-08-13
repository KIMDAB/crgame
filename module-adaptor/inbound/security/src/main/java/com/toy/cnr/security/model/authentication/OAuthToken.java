package com.toy.cnr.security.model.authentication;

import lombok.Getter;

@Getter
public class OAuthToken extends UnAuthentication {

    private final String provider;
    private final String accessToken;

    private OAuthToken(String provider, String accessToken) {
        this.provider = provider;
        this.accessToken = accessToken;
    }

    public static OAuthToken unauthenticated(String provider, String accessToken) {
        return new OAuthToken(provider, accessToken);
    }
}
