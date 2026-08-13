package com.toy.cnr.application.userconsent.service;

import com.toy.cnr.application.common.ResultMapper;
import com.toy.cnr.application.userconsent.mapper.UserConsentMapper;
import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.userconsent.ConsentItem;
import com.toy.cnr.domain.userconsent.UserConsent;
import com.toy.cnr.domain.userconsent.UserConsentCreateCommand;
import com.toy.cnr.port.userconsent.UserConsentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserConsentQueryService {

    private final UserConsentRepository userConsentRepository;

    public UserConsentQueryService(UserConsentRepository userConsentRepository) {
        this.userConsentRepository = userConsentRepository;
    }

    public CommandResult<List<UserConsent>> saveAll(List<UserConsentCreateCommand> commands) {
        var dtos = commands.stream().map(UserConsentMapper::toExternal).toList();
        return ResultMapper.toCommandResult(
            userConsentRepository.saveAll(dtos)
                .map(list -> list.stream().map(UserConsentMapper::toDomain).toList())
        );
    }

    public CommandResult<Boolean> isAllAgreed(Long userId, String deviceId) {
        if (userId != null) {
            return ResultMapper.toCommandResult(userConsentRepository.findByUserId(userId))
                .map(list -> list.stream().map(UserConsentMapper::toDomain).toList())
                .map(this::checkAllAgreed);
        }
        return ResultMapper.toCommandResult(userConsentRepository.findByDeviceId(deviceId))
            .map(list -> list.stream().map(UserConsentMapper::toDomain).toList())
            .map(this::checkAllAgreed);
    }

    private boolean checkAllAgreed(List<UserConsent> consents) {
        Set<ConsentItem> agreedItems = consents.stream()
            .filter(UserConsent::agreed)
            .map(UserConsent::consentItem)
            .collect(Collectors.toSet());
        return agreedItems.containsAll(Set.of(ConsentItem.values()));
    }
}
