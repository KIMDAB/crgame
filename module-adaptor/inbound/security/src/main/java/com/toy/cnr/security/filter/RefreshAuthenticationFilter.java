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

@Component("refreshAuthenticationFilter")
public class RefreshAuthenticationFilter extends UserAuthenticationFilter {

    private AntPathRequestMatcher pathMatcher;

    @Value("${security.refresh.path}")
    private String path;

    @Value("${security.refresh.method}")
    private String method;

    protected RefreshAuthenticationFilter(
        @Qualifier("refreshAuthenticationProvider") AuthenticationProvider authenticationManager,
        @Qualifier("refreshAuthenticationConverter") AuthenticationConverter authenticationConverter,
        @Qualifier("userAuthenticationSuccessHandler") AuthenticationSuccessHandler successHandler,
        @Qualifier("userAuthenticationFailureHandler") AuthenticationFailureHandler failureHandler
    ) {
        super(authenticationManager, authenticationConverter, successHandler, failureHandler);
    }

    @PostConstruct
    public void initialize() {
        this.pathMatcher = new AntPathRequestMatcher(path, method);
    }

    @Override
    protected AntPathRequestMatcher getPathMatcher() {
        return pathMatcher;
    }
}
