package com.toy.cnr.rds.user;

import com.toy.cnr.rds.user.entity.UserAuthOAuthEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthOAuthJpaRepository extends JpaRepository<UserAuthOAuthEntity, Long> {
    Optional<UserAuthOAuthEntity> findByProviderAndOauthId(String provider, String oauthId);
}
