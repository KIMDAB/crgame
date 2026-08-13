package com.toy.cnr.api.auth.usecase;

import com.toy.cnr.api.auth.model.RegisterRequest;
import com.toy.cnr.api.auth.model.RegisterResponse;
import com.toy.cnr.application.user.service.UserAuthLocalService;
import com.toy.cnr.application.user.service.UserService;
import com.toy.cnr.domain.common.CommandResult;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Email 회원가입 오케스트레이터.
 * <p>
 * HTTP 관심사를 모르며, CommandResult 를 반환합니다.
 */
@Component
public class AuthRegisterUseCase {

    private final UserService userService;
    private final UserAuthLocalService userAuthLocalService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthRegisterUseCase(
        UserService userService,
        UserAuthLocalService userAuthLocalService,
        BCryptPasswordEncoder bCryptPasswordEncoder
    ) {
        this.userService = userService;
        this.userAuthLocalService = userAuthLocalService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public CommandResult<RegisterResponse> register(RegisterRequest request) {
        var passwordHash = bCryptPasswordEncoder.encode(request.password());
        return userService.createByEmail(request.email(), request.name())
            .flatMap(user ->
                userAuthLocalService.create(user.id(), passwordHash)
                    .map(ignored -> RegisterResponse.from(user))
            );
    }
}
