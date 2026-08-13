package com.toy.cnr.security.model.response;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public class SuccessResponse<T> extends SecurityResponse {

    private final T content;

    private SuccessResponse(int code, String message, T content) {
        super(true, code, message);
        this.content = content;
    }

    public static <T> SuccessResponse<T> of(T content) {
        return new SuccessResponse<>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), content);
    }
}
