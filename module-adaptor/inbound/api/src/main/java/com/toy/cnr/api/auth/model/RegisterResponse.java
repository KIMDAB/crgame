package com.toy.cnr.api.auth.model;

import com.toy.cnr.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Email 회원가입 응답")
public record RegisterResponse(
    @Schema(description = "유저 ID", example = "1")
    Long id,
    @Schema(description = "이메일", example = "user@example.com")
    String email,
    @Schema(description = "이름", example = "홍길동")
    String name,
    @Schema(description = "가입일시")
    LocalDateTime createdAt
) {
    public static RegisterResponse from(User user) {
        return new RegisterResponse(user.id(), user.email(), user.name(), user.createdAt());
    }
}
