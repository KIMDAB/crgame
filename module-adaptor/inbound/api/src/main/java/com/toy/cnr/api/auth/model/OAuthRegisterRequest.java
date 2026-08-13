package com.toy.cnr.api.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "OAuth 회원가입 요청")
public record OAuthRegisterRequest(
    @Schema(description = "OAuth provider (예: kakao)", example = "kakao", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "provider 는 필수입니다.")
    String provider,

    @Schema(description = "네이티브 SDK 로 발급받은 OAuth access token", example = "ACCESS_TOKEN", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "accessToken 은 필수입니다.")
    String accessToken
) {}
