# naver-map-client

Naver Cloud Platform **Maps API** (API Gateway) 연동 모듈입니다.

## API Gateway 베이스 URL (`naver.map.apigw.*`)

| API | 설정 키 | 베이스 URL |
|-----|---------|------------|
| Static Map | `static-map` | `https://maps.apigw.ntruss.com/map-static/v2` |
| Directions 5 | `direction` | `https://maps.apigw.ntruss.com/map-direction/v1` |
| Directions 15 | `direction-15` | `https://maps.apigw.ntruss.com/map-direction-15/v1` |
| Geocoding | `geocode` | `https://maps.apigw.ntruss.com/map-geocode/v2` |
| Reverse Geocoding | `reverse-geocode` | `https://maps.apigw.ntruss.com/map-reversegeocode/v2` |

코드에서는 `NaverMapProperties.apigw()` 로 주입 후 Feign `url` 등에 사용합니다.

## 패키지

| 패키지 | 용도 |
|--------|------|
| `com.toy.cnr.external.naver.map.client` | Feign 클라이언트 (REST 호출) |
| `com.toy.cnr.external.naver.map.config` | `NaverMapProperties`, 설정 클래스 |
| `com.toy.cnr.external.naver.map.dto` | 요청/응답 DTO |

## 설정

- `application-naver-map.yml` — 베이스 URL + `NAVER_MAP_CLIENT_ID` / `NAVER_MAP_CLIENT_SECRET` (환경 변수)
- `module-bootstrap`의 `application.yml`에서 `spring.config.import`로 포함

## 참고

- [NCP Maps API 개요](https://api.ncloud-docs.com/docs/ko/application-maps-overview)
