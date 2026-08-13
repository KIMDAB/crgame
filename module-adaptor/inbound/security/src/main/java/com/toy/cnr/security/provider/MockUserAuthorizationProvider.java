package com.toy.cnr.security.provider;

import com.toy.cnr.security.model.authentication.AccessToken;
import com.toy.cnr.security.model.authentication.UserPrincipal;
import com.toy.cnr.security.model.detail.AuthenticatedUser;
import com.toy.cnr.security.util.HttpServletRequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("mock")
@Component("userAuthorizationProvider")
public class MockUserAuthorizationProvider implements AuthenticationProvider {

    private static final String UNKNOWN_AUTHORIZATION_EXCEPTION_MESSAGE = "mock 인증 도중 알 수 없는 에러가 발생하였습니다.";

    private final UserDetailsService userDetailsService;

    public MockUserAuthorizationProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            final var user = (AuthenticatedUser) userDetailsService.loadUserByUsername(Strings.EMPTY);
            return UserPrincipal.authenticated(user);
        } catch (Exception e) {
            throw unknownAuthorizationException(e);
        }
    }

    @Override
    public boolean supports(Class<?> tokenClass) {
        return AccessToken.class.isAssignableFrom(tokenClass);
    }

    private AuthenticationException unknownAuthorizationException(Exception e) {
        final var path = HttpServletRequestUtil.getOnlyUrlWithQuery();
        log.error(UNKNOWN_AUTHORIZATION_EXCEPTION_MESSAGE + " unknownAuthorizationException path={} error={}", path, e.getMessage());
        return new AuthenticationServiceException(UNKNOWN_AUTHORIZATION_EXCEPTION_MESSAGE);
    }
}
