package com.toy.cnr.rds.userconsent.entity;

import com.toy.cnr.port.userconsent.model.UserConsentCreateDto;
import com.toy.cnr.port.userconsent.model.UserConsentDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_consent")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserConsentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consent_item", nullable = false)
    private String consentItem;

    @Column(name = "agreed", nullable = false)
    private Boolean agreed;

    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "user_id")
    private Long userId;

    private UserConsentEntity(String consentItem, Boolean agreed, LocalDateTime lastModifiedDate, String deviceId, Long userId) {
        this.consentItem = consentItem;
        this.agreed = agreed;
        this.lastModifiedDate = lastModifiedDate;
        this.deviceId = deviceId;
        this.userId = userId;
    }

    public static UserConsentEntity create(UserConsentCreateDto from) {
        return new UserConsentEntity(from.consentItem(), from.agreed(), LocalDateTime.now(), from.deviceId(), from.userId());
    }

    public void update(UserConsentCreateDto from) {
        this.agreed = from.agreed();
        this.lastModifiedDate = LocalDateTime.now();
        this.userId = from.userId();
    }

    public UserConsentDto toDto() {
        return new UserConsentDto(id, consentItem, agreed, lastModifiedDate, deviceId, userId);
    }
}
