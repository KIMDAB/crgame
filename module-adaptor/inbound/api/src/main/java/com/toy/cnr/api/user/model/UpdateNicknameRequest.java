package com.toy.cnr.api.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "닉네임 변경 요청")
public record UpdateNicknameRequest(
    @Schema(description = "변경할 닉네임", example = "새닉네임")
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    String nickname
) {}
