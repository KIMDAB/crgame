package com.toy.cnr.security.provider;

import com.toy.cnr.security.exception.JwtAuthorizationException;
import com.toy.cnr.security.model.authentication.BearerAuthenticationToken;
import com.toy.cnr.security.model.authentication.RefreshToken;
import com.toy.cnr.security.model.detail.AuthenticatedUser;
import com.toy.cnr.security.util.HttpServletRequestUtil;
import com.toy.cnr.security.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component("refreshAuthenticationProvider")
public class RefreshAuthenticationProvider implements AuthenticationProvider {

    private static final String UNKNOWN_REFRESH_AUTHENTICATION_EXCEPTION_MESSAGE = "인증 도중 알 수 없는 에러가 발생하였습니다.";

    private final UserDetailsService userDetailsService;

    public RefreshAuthenticationProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean supports(Class<?> tokenClass) {
        return RefreshToken.class.isAssignableFrom(tokenClass);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            return internalAuthenticate(authentication);
        } catch (ExpiredJwtException e) {
            return BearerAuthenticationToken.empty();
        } catch (AuthenticationException authenticationException) {
            throw authenticationException;
        } catch (Exception e) {
            throw unknownRefreshAuthenticationException(e);
        }
    }

    private Authentication internalAuthenticate(Authentication authentication) {
        log.debug("RefreshToken authentication token = {}", authentication);
        final var bearer = (RefreshToken) authentication;
        final var refreshToken = bearer.getToken();
        if (JwtUtil.isValid(refreshToken)) {
            final var email = JwtUtil.parseRefreshToken(refreshToken)
                .orElseThrow(this::jwtAuthorizationException);
            final var user = (AuthenticatedUser) userDetailsService.loadUserByUsername(email);
            user.validateAccountStatus();
            final var accessToken = JwtUtil.issueAccessToken(email);
            final var refreshTokenExpiresIn = JwtUtil.parseExpiration(refreshToken)
                .orElseThrow(this::jwtAuthorizationException);
            return BearerAuthenticationToken.authenticated(
                accessToken.getToken(),
                accessToken.getExpiresIn(),
                refreshToken,
                refreshTokenExpiresIn
            );
        } else {
            log.debug("jwt validation failed. refresh token={}", refreshToken);
            throw jwtAuthorizationException();
        }
    }

    private AuthenticationException unknownRefreshAuthenticationException(Exception e) {
        final var path = HttpServletRequestUtil.getOnlyUrlWithQuery();
        log.error(UNKNOWN_REFRESH_AUTHENTICATION_EXCEPTION_MESSAGE + "unknownRefreshAuthenticationException path={} error={}", path, e.getMessage());
        return new AuthenticationServiceException(UNKNOWN_REFRESH_AUTHENTICATION_EXCEPTION_MESSAGE);
    }

    private AuthenticationException jwtAuthorizationException() {
        final var path = HttpServletRequestUtil.getOnlyUrlWithQuery();
        log.debug("jwtAuthorizationException path={}", path);
        return JwtAuthorizationException.of();
    }
}
