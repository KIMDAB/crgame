package com.toy.cnr.security.provider;

import com.toy.cnr.security.model.authentication.BearerAuthenticationToken;
import com.toy.cnr.security.model.detail.AuthenticatedUser;
import com.toy.cnr.security.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component("userAuthenticationProvider")
public class UserAuthenticationProvider implements AuthenticationProvider {

    private static final String UNKNOWN_AUTHENTICATION_EXCEPTION_MESSAGE = "인증 도중 알 수 없는 에러가 발생하였습니다.";

    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserAuthenticationProvider(
        UserDetailsService userDetailsService,
        BCryptPasswordEncoder passwordEncoder
    ) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean supports(Class<?> tokenClass) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(tokenClass);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            return internalAuthenticate(authentication);
        } catch (AuthenticationException authenticationException) {
            throw authenticationException;
        } catch (Exception e) {
            log.error(UNKNOWN_AUTHENTICATION_EXCEPTION_MESSAGE, e);
            throw unknownAuthenticationException();
        }
    }

    private Authentication internalAuthenticate(Authentication authentication) {
        log.debug("UsernamePasswordAuthenticationToken authentication token = {}", authentication);
        final var usernamePassword = (UsernamePasswordAuthenticationToken) authentication;
        final var email = String.valueOf(usernamePassword.getPrincipal());
        final var password = String.valueOf(usernamePassword.getCredentials());
        final var user = (AuthenticatedUser) userDetailsService.loadUserByUsername(email);
        user.validateAccountStatus();
        user.validatePassword(passwordEncoder, password);
        final var accessToken = JwtUtil.issueAccessToken(email);
        final var refreshToken = JwtUtil.issueRefreshToken(email);
        return BearerAuthenticationToken.authenticated(
            accessToken.getToken(),
            accessToken.getExpiresIn(),
            refreshToken.getToken(),
            refreshToken.getExpiresIn()
        );
    }

    private AuthenticationException unknownAuthenticationException() {
        return new AuthenticationServiceException(UNKNOWN_AUTHENTICATION_EXCEPTION_MESSAGE);
    }
}
