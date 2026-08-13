package com.toy.cnr.security.exception;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthorizationException extends AuthenticationException {

    private static final String DEFAULT_ERROR_MESSAGE = "비정상 요청입니다.";

    private JwtAuthorizationException() {
        super(DEFAULT_ERROR_MESSAGE);
    }

    private JwtAuthorizationException(String msg) {
        super(msg);
    }

    public static JwtAuthorizationException of() {
        return new JwtAuthorizationException();
    }

    public static JwtAuthorizationException of(String msg) {
        return new JwtAuthorizationException(msg);
    }
}
