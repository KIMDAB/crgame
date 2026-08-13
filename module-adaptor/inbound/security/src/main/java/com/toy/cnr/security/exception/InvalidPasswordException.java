package com.toy.cnr.security.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class InvalidPasswordException extends UsernameNotFoundException {

    private static final String DEFAULT_ERROR_MESSAGE = "아이디 또는 패스워드를 확인해 주세요.";

    private InvalidPasswordException() {
        super(DEFAULT_ERROR_MESSAGE);
    }

    private InvalidPasswordException(String msg) {
        super(msg);
    }

    public static InvalidPasswordException of() {
        return new InvalidPasswordException();
    }

    public static InvalidPasswordException of(String msg) {
        return new InvalidPasswordException(msg);
    }
}
