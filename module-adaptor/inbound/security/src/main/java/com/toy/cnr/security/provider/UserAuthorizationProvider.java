package com.toy.cnr.security.provider;

import com.toy.cnr.security.exception.JwtAuthorizationException;
import com.toy.cnr.security.model.authentication.AccessToken;
import com.toy.cnr.security.model.authentication.UserPrincipal;
import com.toy.cnr.security.model.detail.AuthenticatedUser;
import com.toy.cnr.security.util.HttpServletRequestUtil;
import com.toy.cnr.security.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("!mock")
@Component("userAuthorizationProvider")
public class UserAuthorizationProvider implements AuthenticationProvider {

    private static final String UNKNOWN_AUTHORIZATION_EXCEPTION_MESSAGE = "인증 도중 알 수 없는 에러가 발생하였습니다.";
    private static final String JWT_EXPIRED_EXCEPTION_MESSAGE = "인증 정보가 만료되었습니다.";

    private final UserDetailsService userDetailsService;

    public UserAuthorizationProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean supports(Class<?> tokenClass) {
        return AccessToken.class.isAssignableFrom(tokenClass);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            return internalAuthenticate(authentication);
        } catch (AuthenticationException authenticationException) {
            throw authenticationException;
        } catch (ExpiredJwtException expired) {
            throw jwtExpiredException();
        } catch (Exception e) {
            throw unknownAuthorizationException(e);
        }
    }

    private Authentication internalAuthenticate(Authentication authentication) {
        log.info("AccessToken authentication token = {}", authentication);
        final var bearer = (AccessToken) authentication;
        final var accessToken = bearer.getToken();
        if (JwtUtil.isValid(accessToken)) {
            final var email = JwtUtil.parseAccessToken(accessToken)
                .orElseThrow(this::jwtAuthorizationException);
            final var user = (AuthenticatedUser) userDetailsService.loadUserByUsername(email);
            user.validateAccountStatus();
            return UserPrincipal.authenticated(user);
        } else {
            log.debug("jwt validation failed. access token={}", accessToken);
            throw jwtAuthorizationException();
        }
    }

    private AuthenticationException unknownAuthorizationException(Exception e) {
        final var path = HttpServletRequestUtil.getOnlyUrlWithQuery();
        log.error(UNKNOWN_AUTHORIZATION_EXCEPTION_MESSAGE + " unknownAuthorizationException path=" + path, e);
        return new AuthenticationServiceException(UNKNOWN_AUTHORIZATION_EXCEPTION_MESSAGE);
    }

    private AuthenticationException jwtExpiredException() {
        final var path = HttpServletRequestUtil.getOnlyUrlWithQuery();
        log.debug("jwtExpiredException path={}", path);
        return new AuthenticationServiceException(JWT_EXPIRED_EXCEPTION_MESSAGE);
    }

    private AuthenticationException jwtAuthorizationException() {
        final var path = HttpServletRequestUtil.getOnlyUrlWithQuery();
        log.debug("jwtAuthorizationException path={}", path);
        return JwtAuthorizationException.of();
    }
}
