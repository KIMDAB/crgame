package com.toy.cnr.api.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Email 회원가입 요청")
public record RegisterRequest(
    @Schema(description = "이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "email 은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email,

    @Schema(description = "비밀번호 (최소 8자)", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "password 는 필수입니다.")
    @Size(min = 8, message = "password 는 최소 8자 이상이어야 합니다.")
    String password,

    @Schema(description = "이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "name 은 필수입니다.")
    String name
) {}
