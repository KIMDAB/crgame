package com.toy.cnr.rds.user;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.user.UserRepository;
import com.toy.cnr.port.user.model.UserCreateDto;
import com.toy.cnr.port.user.model.UserDto;
import com.toy.cnr.rds.user.entity.UserEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryImpl(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public RepositoryResult<UserDto> findByEmail(String email) {
        return RepositoryResult.ofOptional(
            () -> userJpaRepository.findByEmail(email).map(UserEntity::toDto),
            "User not found with email: " + email
        );
    }

    @Override
    public RepositoryResult<UserDto> save(UserCreateDto dto) {
        return RepositoryResult.wrap(() -> {
            var entity = userJpaRepository.save(UserEntity.create(dto));
            return new RepositoryResult.Found<>(entity.toDto());
        });
    }

    @Override
    public RepositoryResult<Void> updateLastLoginAt(String email, LocalDateTime lastLoginAt) {
        return RepositoryResult.ofOptional(
            () -> userJpaRepository.findByEmail(email).map(entity -> {
                entity.updateLastLoginAt(lastLoginAt);
                userJpaRepository.save(entity);
                return null;
            }),
            "User not found with email: " + email
        );
    }

    @Override
    public RepositoryResult<UserDto> updateNickname(String email, String nickname) {
        return RepositoryResult.ofOptional(
            () -> userJpaRepository.findByEmail(email).map(entity -> {
                entity.updateNickname(nickname);
                return userJpaRepository.save(entity).toDto();
            }),
            "User not found with email: " + email
        );
    }
}
