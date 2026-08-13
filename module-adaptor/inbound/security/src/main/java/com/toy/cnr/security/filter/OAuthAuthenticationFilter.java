package com.toy.cnr.security.filter;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

@Component("oAuthAuthenticationFilter")
public class OAuthAuthenticationFilter extends UserAuthenticationFilter {

    @Value("${security.oauth.path}")
    private String oauthPath;

    @Value("${security.oauth.method}")
    private String oauthMethod;

    protected OAuthAuthenticationFilter(
        @Qualifier("oAuthAuthenticationProvider") AuthenticationProvider authenticationProvider,
        @Qualifier("oAuthAuthenticationConverter") AuthenticationConverter authenticationConverter,
        @Qualifier("userAuthenticationSuccessHandler") AuthenticationSuccessHandler successHandler,
        @Qualifier("userAuthenticationFailureHandler") AuthenticationFailureHandler failureHandler
    ) {
        super(authenticationProvider, authenticationConverter, successHandler, failureHandler);
    }

    @PostConstruct
    public void initializeOAuth() {
        this.pathMatcher = new AntPathRequestMatcher(oauthPath, oauthMethod);
    }
}
