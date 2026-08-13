package com.toy.cnr.api.common.security;

import com.toy.cnr.application.user.service.OAuthUserService;
import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.security.exception.InvalidUsernameException;
import com.toy.cnr.security.model.detail.AuthenticatedUser;
import com.toy.cnr.security.port.OAuthUserLoaderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@Profile("!mock")
@Primary
public class OAuthUserLoaderServiceAdaptor implements OAuthUserLoaderService {

    private final OAuthUserService oAuthUserService;

    public OAuthUserLoaderServiceAdaptor(OAuthUserService oAuthUserService) {
        this.oAuthUserService = oAuthUserService;
    }

    @Override
    public AuthenticatedUser loadByOAuthToken(String provider, String accessToken) {
        return switch (oAuthUserService.findByOAuthToken(provider, accessToken)) {
            case CommandResult.Success(var user, var msg) -> AuthenticatedUser.of(
                user.email(),
                "",
                true,
                true,
                true,
                Set.of(),
                user
            );
            case CommandResult.ValidationError(var errors) -> {
                log.error("Validation error during OAuth login, provider : {}, errors : {}", provider, errors);
                throw InvalidUsernameException.of();
            }
            case CommandResult.BusinessError(var reason) -> {
                log.error("Business error during OAuth login, provider : {}, reason : {}", provider, reason);
                throw InvalidUsernameException.of();
            }
        };
    }

}
