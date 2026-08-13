package com.toy.cnr.security.handler;

import com.toy.cnr.security.model.response.FailResponse;
import com.toy.cnr.security.util.HttpServletRequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component("userAuthorizationFailureHandler")
public class UserAuthorizationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException, ServletException {
        log.warn("Authentication request failed. errorMessage={}", exception.getMessage());
        final var errorResponse =  FailResponse.of(
            isAccountException(exception) ? HttpStatus.BAD_REQUEST : HttpStatus.UNAUTHORIZED,
            exception.getMessage()
        );
        HttpServletRequestUtil.flushObject(response, errorResponse.getCode(), errorResponse);
    }

    private boolean isAccountException(AuthenticationException exception) {
        return List.of(
            UsernameNotFoundException.class,
            BadCredentialsException.class,
            AccountExpiredException.class,
            LockedException.class,
            DisabledException.class
        ).contains(exception.getClass());
    }
}
