package com.toy.cnr.api.user.api;

import com.toy.cnr.api.common.util.ResponseMapper;
import com.toy.cnr.api.user.model.UpdateNicknameRequest;
import com.toy.cnr.api.user.model.UpdateNicknameResponse;
import com.toy.cnr.api.user.usecase.UpdateNicknameUseCase;
import com.toy.cnr.security.util.UserPrincipalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "user-api")
@RestController
@RequestMapping("/v1/users")
public class UserApi {

    private final UpdateNicknameUseCase updateNicknameUseCase;

    public UserApi(UpdateNicknameUseCase updateNicknameUseCase) {
        this.updateNicknameUseCase = updateNicknameUseCase;
    }

    @Operation(
        summary = "닉네임 변경",
        description = "로그인된 사용자의 닉네임을 변경합니다.",
        tags = "user-api"
    )
    @PatchMapping("/me/nickname")
    public ResponseEntity<UpdateNicknameResponse> updateNickname(
        @RequestBody @Valid UpdateNicknameRequest request
    ) {
        var email = UserPrincipalUtil.getUser().getUsername();
        return ResponseMapper.toResponseEntity(updateNicknameUseCase.update(email, request));
    }
}
