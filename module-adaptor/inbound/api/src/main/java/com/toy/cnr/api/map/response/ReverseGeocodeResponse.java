package com.toy.cnr.api.map.response;

import com.toy.cnr.port.map.model.ReverseGeocodeDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좌표 역지오코딩 응답")
public record ReverseGeocodeResponse(
    @Schema(description = "도로명 주소", example = "서울특별시 강남구 강남대로 396")
    String roadAddress,
    @Schema(description = "지번 주소", example = "서울특별시 강남구 역삼동 123-45")
    String jibunAddress,
    @Schema(description = "위도", example = "37.4979")
    double latitude,
    @Schema(description = "경도", example = "127.0276")
    double longitude
) {
    public static ReverseGeocodeResponse from(ReverseGeocodeDto dto) {
        return new ReverseGeocodeResponse(
            dto.roadAddress(),
            dto.jibunAddress(),
            dto.latitude(),
            dto.longitude()
        );
    }
}
