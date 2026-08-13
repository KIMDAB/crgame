package com.toy.cnr.security.filter;

import com.toy.cnr.security.provider.PermitMatcherProvider;
import com.toy.cnr.security.wrapper.UserHttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component("userAuthorizationFilter")
public class UserAuthorizationFilter extends OncePerRequestFilter {

    private final PermitMatcherProvider permitMatcherProvider;
    private final AuthenticationManager authenticationManager;
    private final AuthenticationConverter authenticationConverter;
    private final AuthenticationSuccessHandler successHandler;
    private final AuthenticationFailureHandler failureHandler;

    protected UserAuthorizationFilter(
        @Qualifier("permitMatcherProvider") PermitMatcherProvider permitMatcherProvider,
        @Qualifier("userAuthorizationProvider") AuthenticationProvider authenticationProvider,
        @Qualifier("userAuthorizationConverter") AuthenticationConverter authenticationConverter,
        @Qualifier("userAuthorizationSuccessHandler") AuthenticationSuccessHandler successHandler,
        @Qualifier("userAuthorizationFailureHandler") AuthenticationFailureHandler failureHandler
    ) {
        this.permitMatcherProvider = permitMatcherProvider;
        this.authenticationManager = new ProviderManager(authenticationProvider);
        this.authenticationConverter = authenticationConverter;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            final var accessToken = authenticationConverter.convert(request);
            final var principal = authenticationManager.authenticate(accessToken);
            final var wrapper = new UserHttpServletRequest(request, principal);
            successHandler.onAuthenticationSuccess(wrapper, response, filterChain, principal);
        } catch (AuthenticationException failed) {
            failureHandler.onAuthenticationFailure(request, response, failed);
        }
    }

    @Override
    protected boolean shouldNotFilter(
        HttpServletRequest request
    ) throws ServletException {
        final var permitAllMatcher = permitMatcherProvider.getAsList();
        final var requestMatcher = CollectionUtils.isEmpty(permitAllMatcher)
            ? new NegatedRequestMatcher(AnyRequestMatcher.INSTANCE)
            : new OrRequestMatcher(permitAllMatcher);
        return requestMatcher.matches(request);
    }
}
