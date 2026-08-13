package com.toy.cnr.external.kakao;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.oauth.OAuthProviderRepository;
import com.toy.cnr.port.oauth.model.OAuthUserInfoDto;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class KakaoOAuthProviderRepositoryImpl implements OAuthProviderRepository {

    private static final String PROVIDER_KAKAO = "kakao";

    private final KakaoApiClient kakaoApiClient;

    public KakaoOAuthProviderRepositoryImpl(KakaoApiClient kakaoApiClient) {
        this.kakaoApiClient = kakaoApiClient;
    }

    @Override
    public RepositoryResult<OAuthUserInfoDto> fetchUserInfo(String provider, String accessToken) {
        if (!PROVIDER_KAKAO.equalsIgnoreCase(provider)) {
            return new RepositoryResult.NotFound<>("Unsupported provider: " + provider);
        }
        return RepositoryResult.wrap(() -> {
            var userInfo = fetchKakaoUserInfo(accessToken);
            return new RepositoryResult.Found<>(userInfo);
        });
    }

    @SuppressWarnings("unchecked")
    private OAuthUserInfoDto fetchKakaoUserInfo(String accessToken) {
        var response = kakaoApiClient.fetchUserInfo("Bearer " + accessToken);

        var id = String.valueOf(response.get("id"));
        var kakaoAccount = (Map<String, Object>) response.getOrDefault("kakao_account", Map.of());
        var profile = (Map<String, Object>) kakaoAccount.getOrDefault("profile", Map.of());

        var email = (String) kakaoAccount.getOrDefault("email", id + "@kakao.placeholder");
        var nickname = (String) profile.getOrDefault("nickname", "kakao_user");

        return new OAuthUserInfoDto(id, email, nickname);
    }
}
