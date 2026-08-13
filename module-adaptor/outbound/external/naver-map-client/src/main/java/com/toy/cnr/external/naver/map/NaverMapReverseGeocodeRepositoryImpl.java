package com.toy.cnr.external.naver.map;

import com.toy.cnr.external.naver.map.client.NaverReverseGeocodeClient;
import com.toy.cnr.external.naver.map.config.NaverMapProperties;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.map.ReverseGeocodeRepository;
import com.toy.cnr.port.map.model.ReverseGeocodeDto;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class NaverMapReverseGeocodeRepositoryImpl implements ReverseGeocodeRepository {

    private final NaverReverseGeocodeClient reverseGeocodeClient;
    private final NaverMapProperties properties;

    public NaverMapReverseGeocodeRepositoryImpl(
        NaverReverseGeocodeClient reverseGeocodeClient,
        NaverMapProperties properties
    ) {
        this.reverseGeocodeClient = reverseGeocodeClient;
        this.properties = properties;
    }

    @Override
    public RepositoryResult<ReverseGeocodeDto> reverseGeocode(double latitude, double longitude) {
        return RepositoryResult.wrap(() -> {
            var auth = properties.auth();
            if (auth == null || blank(auth.clientId()) || blank(auth.clientSecret())) {
                return new RepositoryResult.Error<>(
                    new IllegalStateException("Naver map auth config is missing")
                );
            }

            String coords = longitude + "," + latitude;
            Map<String, Object> response = reverseGeocodeClient.reverseGeocode(
                auth.clientId(),
                auth.clientSecret(),
                coords,
                "roadaddr,addr",
                "json"
            );

            var addresses = parseAddresses(response);
            String roadAddress = addresses.roadAddress();
            String jibunAddress = addresses.jibunAddress();

            if (blank(roadAddress) && blank(jibunAddress)) {
                return new RepositoryResult.NotFound<>("Address not found for the coordinate");
            }

            return new RepositoryResult.Found<>(
                new ReverseGeocodeDto(roadAddress, jibunAddress, latitude, longitude)
            );
        });
    }

    @SuppressWarnings("unchecked")
    private static ParsedAddresses parseAddresses(Map<String, Object> response) {
        // Naver Reverse Geocoding 응답은 대략 아래 형태입니다.
        // { "results": [ { "name": "roadaddr", "region": {...}, "land": {...} }, { "name": "addr", ... } ] }
        Object resultsObj = response.get("results");
        if (!(resultsObj instanceof List<?> results)) {
            return new ParsedAddresses(null, null);
        }

        String road = null;
        String jibun = null;

        for (Object item : results) {
            if (!(item instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> result = (Map<String, Object>) raw;
            String name = asString(result.get("name"));
            if (blank(name)) {
                continue;
            }

            Map<String, Object> region = asMap(result.get("region"));
            Map<String, Object> land = asMap(result.get("land"));

            String regionStr = formatRegion(region);
            if (name.equalsIgnoreCase("roadaddr")) {
                String roadStr = formatRoad(land);
                if (!blank(regionStr) && !blank(roadStr)) {
                    road = regionStr + " " + roadStr;
                }
            }
            if (name.equalsIgnoreCase("addr")) {
                String jibunStr = formatJibun(region, land);
                if (!blank(jibunStr)) {
                    jibun = jibunStr;
                }
            }
        }

        return new ParsedAddresses(road, jibun);
    }

    private static String formatRegion(Map<String, Object> region) {
        if (region == null) {
            return null;
        }
        // area1: 시/도, area2: 구/군, area3: 동/읍/면
        String area1 = getNestedName(region, "area1");
        String area2 = getNestedName(region, "area2");
        String area3 = getNestedName(region, "area3");
        return joinNonBlank(area1, area2, area3);
    }

    private static String formatRoad(Map<String, Object> land) {
        if (land == null) {
            return null;
        }
        String roadName = asString(land.get("name"));
        String n1 = asString(land.get("number1"));
        String n2 = asString(land.get("number2"));
        String buildingNo = formatNumber(n1, n2);
        return joinNonBlank(roadName, buildingNo);
    }

    private static String formatJibun(Map<String, Object> region, Map<String, Object> land) {
        // 지번은 보통 "시/도 구/군 동 number1-number2" 형태
        String regionStr = formatRegion(region);
        if (land == null) {
            return regionStr;
        }
        String n1 = asString(land.get("number1"));
        String n2 = asString(land.get("number2"));
        String jibunNo = formatNumber(n1, n2);
        return joinNonBlank(regionStr, jibunNo);
    }

    private static String getNestedName(Map<String, Object> parent, String key) {
        Map<String, Object> area = asMap(parent.get(key));
        return area == null ? null : asString(area.get("name"));
    }

    private static String formatNumber(String n1, String n2) {
        if (blank(n1)) {
            return null;
        }
        if (blank(n2)) {
            return n1;
        }
        return n1 + "-" + n2;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> raw) {
            return (Map<String, Object>) raw;
        }
        return null;
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String joinNonBlank(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (blank(p)) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(p.trim());
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    private record ParsedAddresses(String roadAddress, String jibunAddress) {}

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
