package com.toy.cnr.security.provider;

import com.toy.cnr.security.model.authentication.BearerAuthenticationToken;
import com.toy.cnr.security.model.authentication.OAuthToken;
import com.toy.cnr.security.port.OAuthUserLoaderService;
import com.toy.cnr.security.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component("oAuthAuthenticationProvider")
public class OAuthAuthenticationProvider implements AuthenticationProvider {

    private static final String UNKNOWN_AUTHENTICATION_EXCEPTION_MESSAGE = "OAuth 인증 도중 알 수 없는 에러가 발생하였습니다.";

    private final OAuthUserLoaderService oAuthUserLoaderService;

    public OAuthAuthenticationProvider(OAuthUserLoaderService oAuthUserLoaderService) {
        this.oAuthUserLoaderService = oAuthUserLoaderService;
    }

    @Override
    public boolean supports(Class<?> tokenClass) {
        return OAuthToken.class.isAssignableFrom(tokenClass);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            return internalAuthenticate(authentication);
        } catch (AuthenticationException authenticationException) {
            throw authenticationException;
        } catch (Exception e) {
            log.error(UNKNOWN_AUTHENTICATION_EXCEPTION_MESSAGE, e);
            throw new AuthenticationServiceException(UNKNOWN_AUTHENTICATION_EXCEPTION_MESSAGE, e);
        }
    }

    private Authentication internalAuthenticate(Authentication authentication) {
        log.debug("OAuthToken authentication token = {}", authentication);
        var oauthToken = (OAuthToken) authentication;
        var user = oAuthUserLoaderService.loadByOAuthToken(oauthToken.getProvider(), oauthToken.getAccessToken());
        user.validateAccountStatus();
        var accessToken = JwtUtil.issueAccessToken(user.getUsername());
        var refreshToken = JwtUtil.issueRefreshToken(user.getUsername());
        return BearerAuthenticationToken.authenticated(
            accessToken.getToken(),
            accessToken.getExpiresIn(),
            refreshToken.getToken(),
            refreshToken.getExpiresIn()
        );
    }
}
