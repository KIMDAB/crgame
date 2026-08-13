package com.toy.cnr.rds.userconsent;

import com.toy.cnr.rds.userconsent.entity.UserConsentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserConsentJpaRepository extends JpaRepository<UserConsentEntity, Long> {
    Optional<UserConsentEntity> findByDeviceIdAndConsentItem(String deviceId, String consentItem);
    List<UserConsentEntity> findByDeviceId(String deviceId);
    List<UserConsentEntity> findByUserId(Long userId);
}
