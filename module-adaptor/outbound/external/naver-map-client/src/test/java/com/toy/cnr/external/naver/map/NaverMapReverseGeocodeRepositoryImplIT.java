package com.toy.cnr.external.naver.map;

import com.toy.cnr.external.naver.map.client.NaverReverseGeocodeClient;
import com.toy.cnr.external.naver.map.config.NaverMapConfiguration;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.map.ReverseGeocodeRepository;
import com.toy.cnr.port.map.model.ReverseGeocodeDto;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = NaverMapReverseGeocodeRepositoryImplIT.TestApplication.class)
class NaverMapReverseGeocodeRepositoryImplIT {

    @Autowired
    private ReverseGeocodeRepository reverseGeocodeRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("naver.map.apigw.reverse-geocode", () -> "https://maps.apigw.ntruss.com/map-reversegeocode/v2");
        registry.add("naver.map.auth.client-id", () -> System.getenv("NAVER_MAP_CLIENT_ID"));
        registry.add("naver.map.auth.client-secret", () -> System.getenv("NAVER_MAP_CLIENT_SECRET"));
    }

    /*
    ./gradlew :module-adaptor:outbound:external:naver-map-client:test \
  --tests "*NaverMapReverseGeocodeRepositoryImplIT" --info
  */

    @Test
    @DisplayName("강남역 좌표를 역지오코딩하면 도로명/지번 주소가 반환된다")
    void reverseGeocodeGangnamStation() {
        String clientId = System.getenv("NAVER_MAP_CLIENT_ID");
        String clientSecret = System.getenv("NAVER_MAP_CLIENT_SECRET");

        Assumptions.assumeTrue(
            clientId != null && !clientId.isBlank() && clientSecret != null && !clientSecret.isBlank(),
            "NAVER_MAP_CLIENT_ID / NAVER_MAP_CLIENT_SECRET 이 없어 테스트를 건너뜁니다."
        );

        RepositoryResult<ReverseGeocodeDto> result =
            reverseGeocodeRepository.reverseGeocode(37.4979, 127.0276);

        assertTrue(result instanceof RepositoryResult.Found<ReverseGeocodeDto>);
        ReverseGeocodeDto dto = ((RepositoryResult.Found<ReverseGeocodeDto>) result).data();

        // 테스트 실행 시 터미널에서 가공 결과를 확인하기 위한 출력
        System.out.println("[ReverseGeocodeDto] roadAddress=" + dto.roadAddress());
        System.out.println("[ReverseGeocodeDto] jibunAddress=" + dto.jibunAddress());
        System.out.println("[ReverseGeocodeDto] lat=" + dto.latitude() + ", lon=" + dto.longitude());

        boolean hasRoad = dto.roadAddress() != null && !dto.roadAddress().isBlank();
        boolean hasJibun = dto.jibunAddress() != null && !dto.jibunAddress().isBlank();

        assertTrue(hasRoad || hasJibun);
        if (hasRoad) {
            assertFalse(dto.roadAddress().isBlank());
        }
    }

    @EnableAutoConfiguration
    @EnableFeignClients(basePackageClasses = NaverReverseGeocodeClient.class)
    @ComponentScan(basePackageClasses = {
        NaverMapConfiguration.class,
        NaverMapReverseGeocodeRepositoryImpl.class
    })
    static class TestApplication {
    }
}
