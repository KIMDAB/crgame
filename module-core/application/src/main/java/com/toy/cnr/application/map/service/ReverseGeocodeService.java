package com.toy.cnr.application.map.service;

import com.toy.cnr.application.common.ResultMapper;
import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.port.map.ReverseGeocodeRepository;
import com.toy.cnr.port.map.model.ReverseGeocodeDto;
import org.springframework.stereotype.Service;

/**
 * 좌표 -> 주소 변환 애플리케이션 서비스.
 */
@Service
public class ReverseGeocodeService {

    private final ReverseGeocodeRepository reverseGeocodeRepository;

    public ReverseGeocodeService(ReverseGeocodeRepository reverseGeocodeRepository) {
        this.reverseGeocodeRepository = reverseGeocodeRepository;
    }

    public CommandResult<ReverseGeocodeDto> reverseGeocode(double latitude, double longitude) {
        return ResultMapper.toCommandResult(
            reverseGeocodeRepository.reverseGeocode(latitude, longitude)
        );
    }
}
