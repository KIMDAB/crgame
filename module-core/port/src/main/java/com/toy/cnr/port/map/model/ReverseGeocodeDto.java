package com.toy.cnr.port.map.model;

/**
 * 좌표 -> 주소 변환 결과 DTO.
 *
 * @param roadAddress  도로명 주소 (없을 수 있음)
 * @param jibunAddress 지번 주소 (없을 수 있음)
 * @param latitude     위도
 * @param longitude    경도
 */
public record ReverseGeocodeDto(
    String roadAddress,
    String jibunAddress,
    double latitude,
    double longitude
) {}
