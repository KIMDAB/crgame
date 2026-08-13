package com.toy.cnr.domain.user;

import java.time.LocalDateTime;

public record User(
    Long id,
    String email,
    String name,
    String nickname,
    LocalDateTime createdAt
) {
}
