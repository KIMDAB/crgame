package com.toy.cnr.application.user.error;

public class UserNotFoundException extends RuntimeException {

    private static final String DEFAULT_ERROR_MESSAGE = "User 정보를 찾을 수 없습니다.";

    public UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException of() {
        return new UserNotFoundException(DEFAULT_ERROR_MESSAGE);
    }

    public static UserNotFoundException of(String message) {
        return new UserNotFoundException(message);
    }
}