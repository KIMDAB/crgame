package com.toy.cnr.api.map.usecase;

import com.toy.cnr.api.map.response.ReverseGeocodeResponse;
import com.toy.cnr.application.map.service.ReverseGeocodeService;
import com.toy.cnr.domain.common.CommandResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MapUseCase {

    private final ReverseGeocodeService reverseGeocodeService;

    public MapUseCase(ReverseGeocodeService reverseGeocodeService) {
        this.reverseGeocodeService = reverseGeocodeService;
    }

    public CommandResult<ReverseGeocodeResponse> reverseGeocode(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            return new CommandResult.ValidationError<>(
                List.of("latitude must be between -90 and 90")
            );
        }
        if (longitude < -180.0 || longitude > 180.0) {
            return new CommandResult.ValidationError<>(
                List.of("longitude must be between -180 and 180")
            );
        }

        return reverseGeocodeService.reverseGeocode(latitude, longitude)
            .map(ReverseGeocodeResponse::from);
    }
}
