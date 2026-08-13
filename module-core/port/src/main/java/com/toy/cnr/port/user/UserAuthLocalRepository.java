package com.toy.cnr.port.user;

import com.toy.cnr.port.common.RepositoryResult;

public interface UserAuthLocalRepository {
    RepositoryResult<String> findPasswordHashByEmail(String email);
    RepositoryResult<Void> save(Long userId, String passwordHash);
}
