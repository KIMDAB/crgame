package com.toy.cnr.api.map;

import com.toy.cnr.api.common.util.ResponseMapper;
import com.toy.cnr.api.map.response.ReverseGeocodeResponse;
import com.toy.cnr.api.map.usecase.MapUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Map", description = "지도/주소 변환 API")
@RestController
@RequestMapping("/v1/map")
public class MapApi {

    private final MapUseCase mapUseCase;

    public MapApi(MapUseCase mapUseCase) {
        this.mapUseCase = mapUseCase;
    }

    @Operation(
        summary = "좌표를 도로명 주소로 변환",
        description = "프론트에서 전달한 latitude/longitude(좌표)를 Naver Reverse Geocoding으로 변환합니다."
    )
    @GetMapping("/reverse-geocode")
    public ResponseEntity<ReverseGeocodeResponse> reverseGeocode(
        @Parameter(description = "위도", required = true, example = "37.4979")
        @RequestParam double latitude,
        @Parameter(description = "경도", required = true, example = "127.0276")
        @RequestParam double longitude
    ) {
        return ResponseMapper.toResponseEntity(
            mapUseCase.reverseGeocode(latitude, longitude)
        );
    }
}
