package com.toy.cnr.application.user.error;

public class UserUnAuthenticatedException extends RuntimeException {

    private static final String DEFAULT_ERROR_MESSAGE = "인증되지 않은 User 정보 접근을 시도하였습니다.";

    public UserUnAuthenticatedException(String message) {
        super(message);
    }

    public static UserUnAuthenticatedException of() {
        return new UserUnAuthenticatedException(DEFAULT_ERROR_MESSAGE);
    }

    public static UserUnAuthenticatedException of(String message) {
        return new UserUnAuthenticatedException(message);
    }
}