package com.toy.cnr.security.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toy.cnr.security.model.authentication.OAuthToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component("oAuthAuthenticationConverter")
public class OAuthAuthenticationConverter implements AuthenticationConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Authentication convert(HttpServletRequest request) {
        try {
            var body = objectMapper.readValue(request.getInputStream(), Map.class);
            var provider = (String) body.get("provider");
            var accessToken = (String) body.get("accessToken");
            if (provider == null || provider.isBlank()) {
                throw invalidRequestException("provider 가 누락되었습니다.");
            }
            if (accessToken == null || accessToken.isBlank()) {
                throw invalidRequestException("accessToken 이 누락되었습니다.");
            }
            return OAuthToken.unauthenticated(provider.trim(), accessToken.trim());
        } catch (IOException e) {
            throw invalidRequestException("요청 본문을 읽을 수 없습니다.");
        }
    }

    private AuthenticationException invalidRequestException(String message) {
        return new UsernameNotFoundException(message);
    }
}
