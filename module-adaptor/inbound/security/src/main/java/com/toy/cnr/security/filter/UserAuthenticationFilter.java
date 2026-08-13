package com.toy.cnr.security.filter;

import com.toy.cnr.security.model.authentication.BearerAuthenticationToken;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component("userAuthenticationFilter")
public class UserAuthenticationFilter extends OncePerRequestFilter {

    protected AntPathRequestMatcher pathMatcher;

    @Value("${security.authentication.path}")
    private String path;

    @Value("${security.authentication.method}")
    private String method;

    private final AuthenticationManager authenticationManager;
    private final AuthenticationConverter authenticationConverter;
    private final AuthenticationSuccessHandler successHandler;
    private final AuthenticationFailureHandler failureHandler;

    protected UserAuthenticationFilter(
        @Qualifier("userAuthenticationProvider") AuthenticationProvider authenticationProvider,
        @Qualifier("userAuthenticationConverter") AuthenticationConverter authenticationConverter,
        @Qualifier("userAuthenticationSuccessHandler") AuthenticationSuccessHandler successHandler,
        @Qualifier("userAuthenticationFailureHandler") AuthenticationFailureHandler failureHandler
    ) {
        this.authenticationManager = new ProviderManager(authenticationProvider);
        this.authenticationConverter = authenticationConverter;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @PostConstruct
    public void initialize() {
        this.pathMatcher = new AntPathRequestMatcher(path, method);
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (!getPathMatcher().matches(request)) {
            doFilter(request, response, filterChain);
            return;
        }
        try {
            final var authentication = authenticationConverter.convert(request);
            final var bearer = (BearerAuthenticationToken) authenticationManager.authenticate(authentication);
            successHandler.onAuthenticationSuccess(request, response, bearer);
        } catch (AuthenticationException failed) {
            failureHandler.onAuthenticationFailure(request, response, failed);
        }
    }

    protected AntPathRequestMatcher getPathMatcher() {
        return pathMatcher;
    }
}
