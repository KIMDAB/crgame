package com.toy.cnr.external.kakao;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "kakao-api-client", url = "${kakao.oauth.api-host}")
public interface KakaoApiClient {

    @GetMapping(path = "/v2/user/me")
    Map<String, Object> fetchUserInfo(@RequestHeader("Authorization") String authorization);
}
