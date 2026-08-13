package com.toy.cnr.rds.userconsent;

import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.userconsent.UserConsentRepository;
import com.toy.cnr.port.userconsent.model.UserConsentCreateDto;
import com.toy.cnr.port.userconsent.model.UserConsentDto;
import com.toy.cnr.rds.userconsent.entity.UserConsentEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserConsentRepositoryImpl implements UserConsentRepository {

    private final UserConsentJpaRepository userConsentJpaRepository;

    public UserConsentRepositoryImpl(UserConsentJpaRepository userConsentJpaRepository) {
        this.userConsentJpaRepository = userConsentJpaRepository;
    }

    @Override
    public RepositoryResult<List<UserConsentDto>> saveAll(List<UserConsentCreateDto> dtos) {
        return RepositoryResult.wrap(() -> {
            var saved = dtos.stream()
                .map(dto -> {
                    var existing = userConsentJpaRepository.findByDeviceIdAndConsentItem(dto.deviceId(), dto.consentItem());
                    if (existing.isPresent()) {
                        existing.get().update(dto);
                        return userConsentJpaRepository.save(existing.get());
                    }
                    return userConsentJpaRepository.save(UserConsentEntity.create(dto));
                })
                .map(UserConsentEntity::toDto)
                .toList();
            return new RepositoryResult.Found<>(saved);
        });
    }

    @Override
    public RepositoryResult<List<UserConsentDto>> findByDeviceId(String deviceId) {
        return RepositoryResult.wrap(() -> {
            var list = userConsentJpaRepository.findByDeviceId(deviceId).stream()
                .map(UserConsentEntity::toDto)
                .toList();
            return new RepositoryResult.Found<>(list);
        });
    }

    @Override
    public RepositoryResult<List<UserConsentDto>> findByUserId(Long userId) {
        return RepositoryResult.wrap(() -> {
            var list = userConsentJpaRepository.findByUserId(userId).stream()
                .map(UserConsentEntity::toDto)
                .toList();
            return new RepositoryResult.Found<>(list);
        });
    }
}
