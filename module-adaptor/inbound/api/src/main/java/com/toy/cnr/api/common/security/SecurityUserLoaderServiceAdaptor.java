package com.toy.cnr.api.common.security;

import com.toy.cnr.application.user.service.UserAuthLocalService;
import com.toy.cnr.application.user.service.UserService;
import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.user.User;
import com.toy.cnr.security.exception.InvalidUsernameException;
import com.toy.cnr.security.model.detail.AuthenticatedUser;
import com.toy.cnr.security.port.SecurityUserLoaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Service
@Profile("!mock")
@Primary
public class SecurityUserLoaderServiceAdaptor implements SecurityUserLoaderService {

    private final UserService userService;
    private final UserAuthLocalService userAuthLocalService;

    public SecurityUserLoaderServiceAdaptor(
        UserService userService,
        UserAuthLocalService userAuthLocalService
    ) {
        this.userService = userService;
        this.userAuthLocalService = userAuthLocalService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = switch (userService.findByEmail(email)) {
            case CommandResult.Success(var data, var msg) -> data;
            case CommandResult.ValidationError(var errors) -> {
                log.error("Validation error while loading user by email : {}, errors : {}", email, errors);
                throw InvalidUsernameException.of();
            }
            case CommandResult.BusinessError(var reason) -> {
                log.error("Business error while loading user by email : {}, reason : {}", email, reason);
                throw InvalidUsernameException.of();
            }
        };
        var passwordHash = switch (userAuthLocalService.findPasswordHashByEmail(email)) {
            case CommandResult.Success(var hash, var msg) -> hash;
            case CommandResult.ValidationError(var errors) -> {
                log.error("Validation error while loading password hash by email : {}, errors : {}", email, errors);
                throw InvalidUsernameException.of();
            }
            case CommandResult.BusinessError(var reason) -> {
                log.error("Local auth not found for email : {}, reason : {}", email, reason);
                throw InvalidUsernameException.of();
            }
        };
        return createUserDetails(user, passwordHash);
    }

    @Override
    public void updateLastLoginAt(String username, LocalDateTime lastLoginAt) {
        userService.updateLastLoginAt(username, lastLoginAt);
    }

    private UserDetails createUserDetails(User user, String passwordHash) {
        return AuthenticatedUser.of(
            user.email(),
            passwordHash,
            true,
            true,
            true,
            Set.of(),
            user
        );
    }
}
