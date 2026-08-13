package com.toy.cnr.security.port;

import com.toy.cnr.security.model.detail.AuthenticatedUser;

/**
 * Note: OAuth 로그인을 지원하려면 해당 interface 를 구현해야 함.
 */
public interface OAuthUserLoaderService {
    AuthenticatedUser loadByOAuthToken(String provider, String accessToken);
}
