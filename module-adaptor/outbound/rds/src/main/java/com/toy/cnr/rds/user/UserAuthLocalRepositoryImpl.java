package com.toy.cnr.rds.user;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.user.UserAuthLocalRepository;
import com.toy.cnr.rds.user.entity.UserAuthLocalEntity;
import org.springframework.stereotype.Repository;

@Repository
public class UserAuthLocalRepositoryImpl implements UserAuthLocalRepository {

    private final UserAuthLocalJpaRepository userAuthLocalJpaRepository;

    public UserAuthLocalRepositoryImpl(UserAuthLocalJpaRepository userAuthLocalJpaRepository) {
        this.userAuthLocalJpaRepository = userAuthLocalJpaRepository;
    }

    @Override
    public RepositoryResult<String> findPasswordHashByEmail(String email) {
        return RepositoryResult.ofOptional(
            () -> userAuthLocalJpaRepository.findPasswordHashByEmail(email),
            "Local auth not found for email: " + email
        );
    }

    @Override
    public RepositoryResult<Void> save(Long userId, String passwordHash) {
        return RepositoryResult.wrap(() -> {
            userAuthLocalJpaRepository.save(UserAuthLocalEntity.create(userId, passwordHash));
            return new RepositoryResult.Found<>(null);
        });
    }
}
