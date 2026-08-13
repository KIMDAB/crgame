package com.toy.cnr.security.model.response;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

@Getter
@ToString
public class FailResponse extends SecurityResponse {

    private FailResponse(int code, String message) {
        super(false, code, message);
    }

    public static FailResponse of(HttpStatus httpStatus, String message) {
        return new FailResponse(httpStatus.value(), message);
    }

    public static FailResponse of(AuthenticationException exception) {
        final var errorStatus = getErrorStatus(exception);
        return new FailResponse(errorStatus.value(), exception.getMessage());
    }

    private static HttpStatus getErrorStatus(AuthenticationException exception) {
        switch (exception.getClass().getSimpleName()) {
            case "BadCredentialsException":
            case "UsernameNotFoundException":
            case "LockedException":
            case "DisabledException":
            case "AccountExpiredException":
                return HttpStatus.BAD_REQUEST;
            default:
                return HttpStatus.UNAUTHORIZED;
        }
    }
}
