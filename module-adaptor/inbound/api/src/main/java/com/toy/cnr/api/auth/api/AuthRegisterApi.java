package com.toy.cnr.api.auth.api;

import com.toy.cnr.api.auth.model.OAuthRegisterRequest;
import com.toy.cnr.api.auth.model.RegisterRequest;
import com.toy.cnr.api.auth.model.RegisterResponse;
import com.toy.cnr.api.auth.usecase.AuthOAuthRegisterUseCase;
import com.toy.cnr.api.auth.usecase.AuthRegisterUseCase;
import com.toy.cnr.api.common.util.ResponseMapper;
import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.security.model.authentication.BearerAuthenticationToken;
import com.toy.cnr.security.model.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Tag(name = "token-api")
@RestController
@RequestMapping("/v1/auth")
public class AuthRegisterApi {

    @Value("${security.refresh.tokenCookieKey}")
    private String refreshTokenCookieKey;

    private final AuthRegisterUseCase authRegisterUseCase;
    private final AuthOAuthRegisterUseCase authOAuthRegisterUseCase;

    public AuthRegisterApi(AuthRegisterUseCase authRegisterUseCase, AuthOAuthRegisterUseCase authOAuthRegisterUseCase) {
        this.authRegisterUseCase = authRegisterUseCase;
        this.authOAuthRegisterUseCase = authOAuthRegisterUseCase;
    }

    @Operation(
        summary = "Email 회원가입",
        description = "이메일/비밀번호로 회원가입합니다. 가입 후 별도 로그인이 필요합니다.\n"
            + "- 이미 가입된 이메일이면 `409 Conflict` 를 반환합니다.\n",
        tags = "token-api"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "회원가입 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RegisterResponse.class),
                examples = @ExampleObject(
                    value = "{\"id\":1,\"email\":\"user@example.com\",\"name\":\"홍길동\",\"createdAt\":\"2025-01-01T12:00:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "입력값 유효성 오류",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이미 가입된 이메일",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseMapper.toCreatedResponseEntity(authRegisterUseCase.register(request));
    }

    @Operation(
        summary = "OAuth 회원가입",
        description = "네이티브 SDK 로 발급받은 OAuth access token 으로 회원가입하고 즉시 토큰을 발급합니다. 지원 provider: `kakao`\n"
            + "- 이미 가입된 계정이면 동일하게 `200 OK`로 토큰을 발급합니다(로그인 처리).\n",
        tags = "token-api"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "OAuth 회원가입 성공 (토큰 즉시 발급)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SuccessResponse.class),
                examples = @ExampleObject(
                    value = "{\"success\":true,\"code\":200,\"message\":\"OK\","
                        + "\"content\":{\"accessToken\":\"eyJ...\",\"accessTokenExpiresIn\":\"2025-01-01 12:00:00\"}}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "이미 가입된 계정 또는 유효하지 않은 access token",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/oauth/register")
    public ResponseEntity<SuccessResponse<BearerAuthenticationToken>> oauthRegister(
        @RequestBody @Valid OAuthRegisterRequest request,
        HttpServletResponse httpServletResponse
    ) {
        var result = authOAuthRegisterUseCase.register(request.provider().trim(), request.accessToken().trim());
        if (result instanceof CommandResult.Success<BearerAuthenticationToken>(var bearer, var msg)) {
            setRefreshTokenCookie(httpServletResponse, bearer);
        }
        return ResponseMapper.toResponseEntity(result.map(SuccessResponse::of));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, BearerAuthenticationToken bearer) {
        var cookie = new Cookie(refreshTokenCookieKey, bearer.getRefreshToken());
        cookie.setMaxAge((int) ChronoUnit.SECONDS.between(LocalDateTime.now(), bearer.getRefreshTokenExpiresIn()));
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
