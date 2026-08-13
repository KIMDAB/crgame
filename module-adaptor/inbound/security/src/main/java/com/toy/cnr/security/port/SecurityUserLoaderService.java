package com.toy.cnr.security.port;

import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDateTime;

/**
 * Note: security module 을 사용하기 위해서는 해당 interface 를 구현해야 함.
 */
public interface SecurityUserLoaderService extends UserDetailsService {
    void updateLastLoginAt(String username, LocalDateTime lastLoginAt);
}
