package com.toy.cnr.api.userconsent.usecase;

import com.toy.cnr.api.userconsent.request.UserConsentCreateRequest;
import com.toy.cnr.api.userconsent.response.UserConsentResponse;
import com.toy.cnr.application.userconsent.service.UserConsentQueryService;
import com.toy.cnr.domain.common.CommandResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserConsentUseCase {

    private final UserConsentQueryService userConsentQueryService;

    public UserConsentUseCase(UserConsentQueryService userConsentQueryService) {
        this.userConsentQueryService = userConsentQueryService;
    }

    public CommandResult<List<UserConsentResponse>> saveConsents(UserConsentCreateRequest request) {
        return userConsentQueryService.saveAll(request.toCommands())
            .map(list -> list.stream().map(UserConsentResponse::from).toList());
    }

    public CommandResult<Boolean> isAllAgreed(Long userId, String deviceId) {
        return userConsentQueryService.isAllAgreed(userId, deviceId);
    }
}
