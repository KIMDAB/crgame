package com.toy.cnr.rds.user;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.user.UserAuthOAuthRepository;
import com.toy.cnr.port.user.model.UserAuthOAuthCreateDto;
import com.toy.cnr.port.user.model.UserAuthOAuthDto;
import com.toy.cnr.rds.user.entity.UserAuthOAuthEntity;
import org.springframework.stereotype.Repository;

@Repository
public class UserAuthOAuthRepositoryImpl implements UserAuthOAuthRepository {

    private final UserAuthOAuthJpaRepository userAuthOAuthJpaRepository;

    public UserAuthOAuthRepositoryImpl(UserAuthOAuthJpaRepository userAuthOAuthJpaRepository) {
        this.userAuthOAuthJpaRepository = userAuthOAuthJpaRepository;
    }

    @Override
    public RepositoryResult<UserAuthOAuthDto> findByProviderAndOauthId(String provider, String oauthId) {
        return RepositoryResult.ofOptional(
            () -> userAuthOAuthJpaRepository.findByProviderAndOauthId(provider, oauthId)
                .map(e -> new UserAuthOAuthDto(e.getUserId(), e.getProvider(), e.getOauthId(), e.getAccessToken())),
            "OAuth auth not found for provider: " + provider + ", oauthId: " + oauthId
        );
    }

    @Override
    public RepositoryResult<Void> upsert(UserAuthOAuthCreateDto dto) {
        return RepositoryResult.wrap(() -> {
            var existing = userAuthOAuthJpaRepository.findByProviderAndOauthId(dto.provider(), dto.oauthId());
            if (existing.isPresent()) {
                existing.get().updateAccessToken(dto.accessToken());
                userAuthOAuthJpaRepository.save(existing.get());
            } else {
                userAuthOAuthJpaRepository.save(
                    UserAuthOAuthEntity.create(dto.userId(), dto.provider(), dto.oauthId(), dto.accessToken())
                );
            }
            return new RepositoryResult.Found<>(null);
        });
    }
}
