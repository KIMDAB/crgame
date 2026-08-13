package com.toy.cnr.port.userconsent;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.userconsent.model.UserConsentCreateDto;
import com.toy.cnr.port.userconsent.model.UserConsentDto;

import java.util.List;

public interface UserConsentRepository {
    RepositoryResult<List<UserConsentDto>> saveAll(List<UserConsentCreateDto> dtos);
    RepositoryResult<List<UserConsentDto>> findByDeviceId(String deviceId);
    RepositoryResult<List<UserConsentDto>> findByUserId(Long userId);
}
