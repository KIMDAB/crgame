package com.toy.cnr.security.converter;

import com.toy.cnr.security.model.authentication.AccessToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component("userAuthorizationConverter")
public class UserAuthorizationConverter implements AuthenticationConverter {

    @Value("${security.authorization.bearerDelimiterKey}")
    private String bearerDelimiterKey;

    @Override
    public Authentication convert(HttpServletRequest request) {
        final String bearer = obtainBearerToken(request);
        return AccessToken.unauthenticated(bearer);
    }

    private String obtainBearerToken(HttpServletRequest request) {
        final var authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        return Optional.ofNullable(authorizationHeader)
            .filter(StringUtils::isNoneBlank)
            .filter(authz -> authz.startsWith(getBearerDelimiterKey()))
            .map(authz -> authz.replaceFirst(getBearerDelimiterKey(), ""))
            .map(String::trim)
            .orElse(Strings.EMPTY);
    }

    private String getBearerDelimiterKey() {
        return bearerDelimiterKey + " ";
    }
}
