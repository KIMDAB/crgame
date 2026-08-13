package com.toy.cnr.security.handler;

import com.toy.cnr.security.model.response.FailResponse;
import com.toy.cnr.security.util.HttpServletRequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component("userAuthenticationFailureHandler")
public class UserAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException, ServletException {
        log.warn("Authentication request failed. errorMessage={}", exception.getMessage());
        final var errorResponse =  FailResponse.of(exception);
        HttpServletRequestUtil.flushObject(response, errorResponse.getCode(), errorResponse);
    }
}
