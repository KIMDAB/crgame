package com.toy.cnr.application.userconsent.mapper;

import com.toy.cnr.domain.userconsent.ConsentItem;
import com.toy.cnr.domain.userconsent.UserConsent;
import com.toy.cnr.domain.userconsent.UserConsentCreateCommand;
import com.toy.cnr.port.userconsent.model.UserConsentCreateDto;
import com.toy.cnr.port.userconsent.model.UserConsentDto;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserConsentMapper {

    public static UserConsent toDomain(UserConsentDto dto) {
        return new UserConsent(
            dto.id(),
            ConsentItem.valueOf(dto.consentItem()),
            dto.agreed(),
            dto.lastModifiedDate(),
            dto.deviceId(),
            dto.userId()
        );
    }

    public static UserConsentCreateDto toExternal(UserConsentCreateCommand command) {
        return new UserConsentCreateDto(
            command.consentItem().name(),
            command.agreed(),
            command.deviceId(),
            command.userId()
        );
    }
}
