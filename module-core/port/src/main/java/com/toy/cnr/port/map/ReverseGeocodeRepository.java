package com.toy.cnr.port.map;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.map.model.ReverseGeocodeDto;

/**
 * 좌표를 도로명/지번 주소로 변환하는 외부 API 포트.
 */
public interface ReverseGeocodeRepository {

    RepositoryResult<ReverseGeocodeDto> reverseGeocode(double latitude, double longitude);
}
