package com.toy.cnr.security.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class InvalidUsernameException extends UsernameNotFoundException {

    private static final String DEFAULT_ERROR_MESSAGE = "아이디 또는 패스워드를 확인해 주세요.";

    private InvalidUsernameException() {
        super(DEFAULT_ERROR_MESSAGE);
    }

    private InvalidUsernameException(String msg) {
        super(msg);
    }

    public static InvalidUsernameException of() {
        return new InvalidUsernameException();
    }

    public static InvalidUsernameException of(String msg) {
        return new InvalidUsernameException(msg);
    }
}
