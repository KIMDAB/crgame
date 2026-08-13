package com.toy.cnr.api.auth.api;

import com.toy.cnr.api.auth.model.OAuthRegisterRequest;
import com.toy.cnr.security.model.authentication.BearerAuthenticationToken;
import com.toy.cnr.security.model.response.FailResponse;
import com.toy.cnr.security.model.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1/auth")
public class AuthDocsApi {

    @Operation(
        summary = "token 발급",
        description = "이메일/비밀번호로 인증하여 accessToken 과 refreshToken 을 발급합니다.\n"
            + "- `accessToken` 은 응답 body 에 포함됩니다.\n"
            + "- `refreshToken` 은 http-only cookie 로 설정됩니다.\n"
            + "```\n"
            + "curl --location --request POST 'https://{{url}}/v1/auth/token' \\\n"
            + "--form 'email=\"user@email.com\"' \\\n"
            + "--form 'password=\"CREDENTIAL\"'"
            + "```\n",
        tags = "token-api"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "토큰 발급 성공",
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
            description = "이메일 또는 비밀번호 불일치",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FailResponse.class),
                examples = @ExampleObject(
                    value = "{\"success\":false,\"code\":400,\"message\":\"자격 증명에 실패하였습니다.\"}"
                )
            )
        )
    })
    @PostMapping("/token")
    private ResponseEntity<SuccessResponse<BearerAuthenticationToken>> accessToken(
        @RequestParam("email") String email,
        @RequestParam("password") String password
    ) throws IllegalAccessException {
        throw throwByApiDocsMethodAccess();
    }

    @Operation(
        summary = "OAuth 로그인",
        description = "네이티브 SDK 로 발급받은 OAuth access token 으로 인증 토큰을 발급합니다. 지원 provider: `kakao`\n"
            + "- 가입되지 않은 계정이면 `400 Bad Request` 를 반환합니다. 먼저 `/v1/auth/oauth/register` 로 가입하세요.\n"
            + "```\n"
            + "curl --location --request POST 'https://{{url}}/v1/auth/oauth' \\\n"
            + "--header 'Content-Type: application/json' \\\n"
            + "--data-raw '{\n"
            + "    \"provider\": \"kakao\",\n"
            + "    \"accessToken\": \"ACCESS_TOKEN\"\n"
            + "}'"
            + "```\n",
        tags = "token-api",
        requestBody = @RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OAuthRegisterRequest.class),
                examples = @ExampleObject(
                    name = "kakao",
                    summary = "카카오 OAuth 로그인",
                    value = "{\"provider\": \"kakao\", \"accessToken\": \"ACCESS_TOKEN\"}"
                )
            )
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "OAuth 로그인 성공",
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
            description = "provider 또는 accessToken 누락 / 유효하지 않은 access token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FailResponse.class),
                examples = @ExampleObject(
                    value = "{\"success\":false,\"code\":400,\"message\":\"accessToken 이 누락되었습니다.\"}"
                )
            )
        )
    })
    @PostMapping("/oauth")
    private ResponseEntity<SuccessResponse<BearerAuthenticationToken>> oauthToken() throws IllegalAccessException {
        throw throwByApiDocsMethodAccess();
    }

    @Operation(
        summary = "token 재 발급",
        description = "http-only cookie 의 `refreshToken` 을 사용하여 새로운 accessToken 과 refreshToken 을 재 발급합니다.\n"
            + "- 요청 body 없이 cookie 만으로 인증합니다.\n"
            + "- 재 발급된 `refreshToken` 은 http-only cookie 로 갱신됩니다.\n"
            + "```\n"
            + "curl --location --request POST 'https://{{url}}/v1/auth/refresh' \\\n"
            + "--cookie 'refreshToken=eyJ...'"
            + "```\n",
        tags = "token-api"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "토큰 재 발급 성공",
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
            responseCode = "401",
            description = "refreshToken 이 없거나 유효하지 않음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FailResponse.class),
                examples = @ExampleObject(
                    value = "{\"success\":false,\"code\":401,\"message\":\"인증에 실패하였습니다.\"}"
                )
            )
        )
    })
    @PostMapping("/refresh")
    private ResponseEntity<SuccessResponse<BearerAuthenticationToken>> refreshToken() throws IllegalAccessException {
        throw throwByApiDocsMethodAccess();
    }

    private IllegalAccessException throwByApiDocsMethodAccess() {
        return new IllegalAccessException("This method is for using api documentation. It is not valid.");
    }

}
