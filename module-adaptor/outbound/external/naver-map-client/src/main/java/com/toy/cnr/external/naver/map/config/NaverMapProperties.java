package com.toy.cnr.external.naver.map.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * NCP Maps API Gateway 베이스 URL 및 인증 정보.
 * <p>
 * 요청 헤더: {@code X-NCP-APIGW-API-KEY-ID}, {@code X-NCP-APIGW-API-KEY} (공식 문서 기준)
 */
@ConfigurationProperties(prefix = "naver.map")
public record NaverMapProperties(
    Apigw apigw,
    Auth auth
) {

    /**
     * 각 API별 API Gateway 베이스 URL (Feign {@code url} 등에 사용).
     */
    public record Apigw(
        String staticMap,
        String direction,
        String direction15,
        String geocode,
        String reverseGeocode
    ) {}

    /**
     * NCP 콘솔에서 발급한 Client ID / Client Secret.
     */
    public record Auth(
        String clientId,
        String clientSecret
    ) {}
}
