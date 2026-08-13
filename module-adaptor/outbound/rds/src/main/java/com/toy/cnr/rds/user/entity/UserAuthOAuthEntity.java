package com.toy.cnr.rds.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "user_auth_oauth",
    uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "oauth_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAuthOAuthEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "oauth_id", nullable = false)
    private String oauthId;

    @Column(name = "access_token")
    private String accessToken;

    private UserAuthOAuthEntity(Long userId, String provider, String oauthId, String accessToken) {
        this.userId = userId;
        this.provider = provider;
        this.oauthId = oauthId;
        this.accessToken = accessToken;
    }

    public static UserAuthOAuthEntity create(Long userId, String provider, String oauthId, String accessToken) {
        return new UserAuthOAuthEntity(userId, provider, oauthId, accessToken);
    }

    public void updateAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
