package com.toy.cnr.port.user;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.user.model.UserAuthOAuthCreateDto;
import com.toy.cnr.port.user.model.UserAuthOAuthDto;

public interface UserAuthOAuthRepository {
    RepositoryResult<UserAuthOAuthDto> findByProviderAndOauthId(String provider, String oauthId);
    RepositoryResult<Void> upsert(UserAuthOAuthCreateDto dto);
}
