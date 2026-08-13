package com.toy.cnr.application.game.util;

import com.toy.cnr.domain.room.GeoPoint;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Random;

/**
 * GPS 폴리곤 관련 유틸리티.
 * <p>
 * Ray Casting 알고리즘으로 점이 폴리곤 내부에 있는지 판별합니다.
 */
@UtilityClass
public class PolygonUtils {

    private static final int MAX_RANDOM_ATTEMPTS = 20;

    /**
     * 점(lat, lon)이 폴리곤 내부에 있는지 Ray Casting으로 판별합니다.
     *
     * @param polygon 폴리곤 꼭짓점 목록 (순서 있음)
     * @param lat     위도
     * @param lon     경도
     * @return 내부이면 true
     */
    public static boolean contains(List<GeoPoint> polygon, double lat, double lon) {
        if (polygon == null || polygon.size() < 3) {
            return false;
        }
        int n = polygon.size();
        boolean inside = false;
        int j = n - 1;
        for (int i = 0; i < n; i++) {
            double xi = polygon.get(i).longitude();
            double yi = polygon.get(i).latitude();
            double xj = polygon.get(j).longitude();
            double yj = polygon.get(j).latitude();

            boolean intersect = ((yi > lat) != (yj > lat))
                && (lon < (xj - xi) * (lat - yi) / (yj - yi) + xi);
            if (intersect) {
                inside = !inside;
            }
            j = i;
        }
        return inside;
    }

    /**
     * 폴리곤 내부의 랜덤 좌표를 반환합니다.
     * 바운딩 박스 내 랜덤 점을 생성하고 폴리곤 포함 여부를 검사합니다.
     *
     * @param polygon 폴리곤 꼭짓점 목록
     * @param random  Random 인스턴스
     * @return 폴리곤 내부의 GeoPoint, MAX_ATTEMPTS 초과 시 null
     */
    public static GeoPoint randomPoint(List<GeoPoint> polygon, Random random) {
        if (polygon == null || polygon.size() < 3) {
            return null;
        }
        double minLat = polygon.stream().mapToDouble(GeoPoint::latitude).min().orElseThrow();
        double maxLat = polygon.stream().mapToDouble(GeoPoint::latitude).max().orElseThrow();
        double minLon = polygon.stream().mapToDouble(GeoPoint::longitude).min().orElseThrow();
        double maxLon = polygon.stream().mapToDouble(GeoPoint::longitude).max().orElseThrow();

        for (int i = 0; i < MAX_RANDOM_ATTEMPTS; i++) {
            double lat = minLat + random.nextDouble() * (maxLat - minLat);
            double lon = minLon + random.nextDouble() * (maxLon - minLon);
            if (contains(polygon, lat, lon)) {
                return new GeoPoint(lat, lon);
            }
        }
        return null;
    }
}
