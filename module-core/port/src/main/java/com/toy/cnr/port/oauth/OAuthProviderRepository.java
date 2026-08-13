package com.toy.cnr.port.oauth;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.oauth.model.OAuthUserInfoDto;

public interface OAuthProviderRepository {
    RepositoryResult<OAuthUserInfoDto> fetchUserInfo(String provider, String accessToken);
}
