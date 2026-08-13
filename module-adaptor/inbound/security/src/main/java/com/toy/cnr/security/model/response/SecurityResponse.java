package com.toy.cnr.security.model.response;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SecurityResponse {

    private final boolean success;
    private final int code;
    private final String message;

    protected SecurityResponse(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}
