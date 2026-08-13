package com.toy.cnr.domain.room;

import java.util.List;

/**
 * 게임 맵 구역 정보.
 *
 * @param rallyPoint     집결지 (단일 포인트)
 * @param playArea       전체 활동 구역 (폴리곤 - 위경도 목록)
 * @param prisonArea     감옥 구역 (폴리곤 - 위경도 목록)
 * @param restrictedArea 제한 구역 (폴리곤 - 위경도 목록, null 가능)
 */
public record MapZone(
    GeoPoint rallyPoint,
    List<GeoPoint> playArea,
    List<GeoPoint> prisonArea,
    List<GeoPoint> restrictedArea
) {}
