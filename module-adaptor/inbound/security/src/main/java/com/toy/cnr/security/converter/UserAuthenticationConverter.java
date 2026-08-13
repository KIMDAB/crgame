package com.toy.cnr.security.converter;

import com.toy.cnr.security.util.EmailUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("userAuthenticationConverter")
public class UserAuthenticationConverter implements AuthenticationConverter {

    @Value("${security.authentication.usernameParameterKey}")
    private String usernameParameterKey;

    @Value("${security.authentication.passwordParameterKey}")
    private String passwordParameterKey;

    @Override
    public Authentication convert(HttpServletRequest request) {
        final String email = obtainUsername(request);
        final String password = obtainPassword(request);
        return UsernamePasswordAuthenticationToken
            .unauthenticated(email, password);
    }

    private String obtainUsername(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(usernameParameterKey))
            .map(String::trim)
            .filter(EmailUtil::isValid)
            .orElseThrow(this::throwByObtainUsername);
    }

    private String obtainPassword(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(passwordParameterKey))
            .map(String::trim)
            .orElse(Strings.EMPTY);
    }

    private AuthenticationException throwByObtainUsername() {
        return new UsernameNotFoundException("잘못된 이메일 형식 입니다.");
    }
}
