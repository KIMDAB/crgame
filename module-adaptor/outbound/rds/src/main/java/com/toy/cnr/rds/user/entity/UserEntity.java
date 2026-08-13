package com.toy.cnr.rds.user.entity;

import com.toy.cnr.port.user.model.UserCreateDto;
import com.toy.cnr.port.user.model.UserDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "nickname")
    private String nickname;

    private UserEntity(String email, String name) {
        this.email = email;
        this.name = name;
    }

    public static UserEntity create(UserCreateDto dto) {
        return new UserEntity(dto.email(), dto.name());
    }

    public void updateLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public UserDto toDto() {
        return new UserDto(id, email, name, nickname, createdAt);
    }
}
