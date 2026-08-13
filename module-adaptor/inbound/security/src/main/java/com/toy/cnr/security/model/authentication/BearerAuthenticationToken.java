package com.toy.cnr.security.model.authentication;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class BearerAuthenticationToken extends UnAuthentication {

    @JsonProperty("accessToken")
    private final String accessToken;

    @JsonProperty("accessTokenExpiresIn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private final LocalDateTime accessTokenExpiresIn;

    @JsonIgnore
    private final String refreshToken;

    @JsonIgnore
    private final LocalDateTime refreshTokenExpiresIn;

    private BearerAuthenticationToken(
        String accessToken,
        LocalDateTime accessTokenExpiresIn,
        String refreshToken,
        LocalDateTime refreshTokenExpiresIn
    ) {
        this.accessToken = accessToken;
        this.accessTokenExpiresIn = accessTokenExpiresIn;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    @JsonIgnore
    public static BearerAuthenticationToken authenticated(
        String accessToken,
        LocalDateTime accessTokenExpiresIn,
        String refreshToken,
        LocalDateTime refreshTokenExpiresIn
    ) {
        return new BearerAuthenticationToken(
            accessToken,
            accessTokenExpiresIn,
            refreshToken,
            refreshTokenExpiresIn
        );
    }

    @JsonIgnore
    public static BearerAuthenticationToken empty() {
        return new BearerAuthenticationToken(null, null, null, null);
    }
}
