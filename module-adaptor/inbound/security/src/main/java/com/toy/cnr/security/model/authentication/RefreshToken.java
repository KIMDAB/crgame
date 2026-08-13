package com.toy.cnr.security.model.authentication;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RefreshToken extends UnAuthentication {

    private final String token;

    private RefreshToken(String token) {
        this.token = token;
    }

    public static RefreshToken unauthenticated(String token) {
        return new RefreshToken(token);
    }
}
