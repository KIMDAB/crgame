package com.toy.cnr.security.port;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DefaultSecurityUserLoaderService implements SecurityUserLoaderService {

    @Override
    public void updateLastLoginAt(String username, LocalDateTime lastLoginAt) {

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
