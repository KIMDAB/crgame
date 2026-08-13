package com.toy.cnr.api.auth.usecase;

import com.toy.cnr.application.user.service.OAuthUserService;
import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.security.model.authentication.BearerAuthenticationToken;
import com.toy.cnr.security.util.JwtUtil;
import org.springframework.stereotype.Component;

/**
 * OAuth 회원가입 오케스트레이터.
 * <p>
 * 가입 즉시 토큰을 발급합니다.
 */
@Component
public class AuthOAuthRegisterUseCase {

    private final OAuthUserService oAuthUserService;

    public AuthOAuthRegisterUseCase(OAuthUserService oAuthUserService) {
        this.oAuthUserService = oAuthUserService;
    }

    public CommandResult<BearerAuthenticationToken> register(String provider, String oauthAccessToken) {
        return oAuthUserService.registerOrSignInByOAuthToken(provider, oauthAccessToken)
            .map(user -> {
                var accessToken = JwtUtil.issueAccessToken(user.email());
                var refreshToken = JwtUtil.issueRefreshToken(user.email());
                return BearerAuthenticationToken.authenticated(
                    accessToken.getToken(),
                    accessToken.getExpiresIn(),
                    refreshToken.getToken(),
                    refreshToken.getExpiresIn()
                );
            });
    }
}
