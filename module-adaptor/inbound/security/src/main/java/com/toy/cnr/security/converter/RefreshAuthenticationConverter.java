package com.toy.cnr.security.converter;

import com.toy.cnr.security.exception.JwtAuthorizationException;
import com.toy.cnr.security.model.authentication.RefreshToken;
import com.toy.cnr.security.util.HttpServletRequestUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;

@Slf4j
@Component("refreshAuthenticationConverter")
public class RefreshAuthenticationConverter implements AuthenticationConverter {

    @Value("${security.refresh.tokenCookieKey}")
    private String refreshTokenCookieKey;

    @Override
    public Authentication convert(HttpServletRequest request) {
        final String bearer = obtainRefreshToken(request);
        return RefreshToken.unauthenticated(bearer);
    }

    private String obtainRefreshToken(HttpServletRequest request) {
        return HttpServletRequestUtil.getCookie(request, refreshTokenCookieKey)
           .map(Cookie::getValue)
           .orElseThrow(this::throwByObtainAccessToken);
    }

    private AuthenticationException throwByObtainAccessToken() {
        final var path = HttpServletRequestUtil.getOnlyUrlWithQuery();
        final var cookies = HttpServletRequestUtil.getCookiesAsString();
        log.error("throwByObtainAccessToken path={} cookies={}", path, cookies);
        return JwtAuthorizationException.of();
    }
}
