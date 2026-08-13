package com.toy.cnr.rds.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_auth_local")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAuthLocalEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private UserAuthLocalEntity(Long userId, String passwordHash) {
        this.userId = userId;
        this.passwordHash = passwordHash;
    }

    public static UserAuthLocalEntity create(Long userId, String passwordHash) {
        return new UserAuthLocalEntity(userId, passwordHash);
    }
}
