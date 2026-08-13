package com.toy.cnr.api.user.usecase;

import com.toy.cnr.api.user.model.UpdateNicknameRequest;
import com.toy.cnr.api.user.model.UpdateNicknameResponse;
import com.toy.cnr.application.user.service.UserService;
import com.toy.cnr.domain.common.CommandResult;
import org.springframework.stereotype.Component;

/**
 * 닉네임 변경 오케스트레이터.
 * <p>
 * HTTP 관심사를 모르며, CommandResult 를 반환합니다.
 */
@Component
public class UpdateNicknameUseCase {

    private final UserService userService;

    public UpdateNicknameUseCase(UserService userService) {
        this.userService = userService;
    }

    public CommandResult<UpdateNicknameResponse> update(String email, UpdateNicknameRequest request) {
        return userService.updateNickname(email, request.nickname())
            .map(UpdateNicknameResponse::from);
    }
}
