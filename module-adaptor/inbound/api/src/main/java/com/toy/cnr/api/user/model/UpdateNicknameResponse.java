package com.toy.cnr.api.user.model;

import com.toy.cnr.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 변경 응답")
public record UpdateNicknameResponse(
    @Schema(description = "유저 ID", example = "1")
    Long id,
    @Schema(description = "이름", example = "홍길동")
    String name,
    @Schema(description = "닉네임", example = "새닉네임")
    String nickname
) {
    public static UpdateNicknameResponse from(User user) {
        return new UpdateNicknameResponse(user.id(), user.name(), user.nickname());
    }
}
