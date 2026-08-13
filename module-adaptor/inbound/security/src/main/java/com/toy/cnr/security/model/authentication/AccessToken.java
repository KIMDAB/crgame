package com.toy.cnr.security.model.authentication;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class AccessToken extends UnAuthentication {

    private final String token;

    private AccessToken(String token) {
        this.token = token;
    }

    public static AccessToken unauthenticated(String token) {
        return new AccessToken(token);
    }
}
