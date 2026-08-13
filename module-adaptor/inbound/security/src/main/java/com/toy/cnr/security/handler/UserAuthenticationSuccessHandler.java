package com.toy.cnr.security.handler;

import com.toy.cnr.security.model.authentication.BearerAuthenticationToken;
import com.toy.cnr.security.model.response.SuccessResponse;
import com.toy.cnr.security.port.SecurityUserLoaderService;
import com.toy.cnr.security.util.HttpServletRequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Component("userAuthenticationSuccessHandler")
public class UserAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${security.refresh.tokenCookieKey}")
    private String refreshTokenCookieKey;

    @Value("${security.authentication.usernameParameterKey}")
    private String usernameParameterKey;

    private final SecurityUserLoaderService userService;

    public UserAuthenticationSuccessHandler(SecurityUserLoaderService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        final var bearer = (BearerAuthenticationToken) authentication;
        final var bearerResponse = SuccessResponse.of(bearer);
        if (nonNull(bearer.getRefreshToken())) {
            setRefreshTokenInCookieWithHttpOnly(response, bearer);
            updateLastLoginAt(request);
        }
        HttpServletRequestUtil.flushObject(response, HttpServletResponse.SC_OK, bearerResponse);
    }

    private void updateLastLoginAt(HttpServletRequest request) {
        Optional.ofNullable(request.getParameter(usernameParameterKey))
            .map(String::trim)
            .ifPresent(email -> userService.updateLastLoginAt(email, LocalDateTime.now()));
    }

    private void setRefreshTokenInCookieWithHttpOnly(
        HttpServletResponse response,
        BearerAuthenticationToken bearer
    ) {
        final var refreshToken = bearer.getRefreshToken();
        final var expired = toSecondsFromNow(bearer.getRefreshTokenExpiresIn());
        final var cookie = new Cookie(refreshTokenCookieKey, refreshToken);
        cookie.setMaxAge((int) expired);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private long toSecondsFromNow(LocalDateTime ldt) {
        final var now = LocalDateTime.now();
        return ChronoUnit.SECONDS.between(now, ldt);
    }
}
