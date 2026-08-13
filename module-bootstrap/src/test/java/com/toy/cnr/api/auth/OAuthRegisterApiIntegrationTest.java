package com.toy.cnr.api.auth;

import com.toy.cnr.AbstractIntegrationTest;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.oauth.OAuthProviderRepository;
import com.toy.cnr.port.oauth.model.OAuthUserInfoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OAuthRegisterApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OAuthProviderRepository oAuthProviderRepository;

    private static final String URL = "/v1/auth/oauth/register";

    private static final String PROVIDER = "kakao";
    private static final String CODE = "valid-auth-code";
    private static final String STATE = "csrf-state-value";
    private static final String OAUTH_ID = "kakao-99999";
    private static final String EMAIL = "kakaouser@kakao.com";
    private static final String NAME = "카카오유저";

    @DynamicPropertySource
    static void authProperties(DynamicPropertyRegistry registry) {
        registry.add("jwt.private", () -> "integration-test-jwt-secret-key-32chars-min");
    }

    private static OAuthUserInfoDto oAuthUserInfoDto() {
        return new OAuthUserInfoDto(OAUTH_ID, EMAIL, NAME);
    }

    private String requestBody(String provider, String code, String state) {
        return """
            {
                "provider": "%s",
                "code": "%s",
                "state": "%s"
            }
            """.formatted(provider, code, state);
    }

    @Nested
    @DisplayName("입력값 검증")
    class Validation {

        @Test
        @DisplayName("[실패] state 누락 → 400 Bad Request")
        void missingState_returns400() throws Exception {
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "provider": "kakao",
                            "code": "valid-code"
                        }
                        """))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[실패] state 빈 문자열 → 400 Bad Request")
        void blankState_returns400() throws Exception {
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody(PROVIDER, CODE, "")))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[실패] code 누락 → 400 Bad Request")
        void missingCode_returns400() throws Exception {
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "provider": "kakao",
                            "state": "some-state"
                        }
                        """))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[실패] provider 누락 → 400 Bad Request")
        void missingProvider_returns400() throws Exception {
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "code": "valid-code",
                            "state": "some-state"
                        }
                        """))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("신규 회원 자동 가입")
    class NewUserSignUp {

        @Test
        @DisplayName("[성공] 신규 카카오 유저 → 200 OK + accessToken 포함 응답 + Refresh Token 쿠키")
        void newUser_returns200WithTokens() throws Exception {
            when(oAuthProviderRepository.fetchUserInfo(PROVIDER, CODE))
                .thenReturn(new RepositoryResult.Found<>(oAuthUserInfoDto()));

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody(PROVIDER, CODE, STATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.content.accessTokenExpiresIn").isNotEmpty())
                .andExpect(cookie().exists("refreshToken"));
        }

        @Test
        @DisplayName("[성공] 동일 유저 재요청 → 200 OK (기존 회원 로그인 처리)")
        void existingUser_returns200() throws Exception {
            when(oAuthProviderRepository.fetchUserInfo(PROVIDER, CODE))
                .thenReturn(new RepositoryResult.Found<>(oAuthUserInfoDto()));

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody(PROVIDER, CODE, STATE)))
                .andExpect(status().isOk());

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody(PROVIDER, CODE, STATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.accessToken").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("Kakao API 실패")
    class KakaoApiFailure {

        @Test
        @DisplayName("[실패] Kakao API NotFound → 에러 응답 반환")
        void kakaoApi_notFound_returnsError() throws Exception {
            when(oAuthProviderRepository.fetchUserInfo(any(), any()))
                .thenReturn(new RepositoryResult.NotFound<>("Invalid auth code"));

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody(PROVIDER, CODE, STATE)))
                .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("[실패] Kakao API 예외 → 에러 응답 반환")
        void kakaoApi_throws_returnsError() throws Exception {
            when(oAuthProviderRepository.fetchUserInfo(any(), any()))
                .thenReturn(new RepositoryResult.Error<>(new RuntimeException("Kakao server error")));

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody(PROVIDER, CODE, STATE)))
                .andExpect(status().is4xxClientError());
        }
    }
}
