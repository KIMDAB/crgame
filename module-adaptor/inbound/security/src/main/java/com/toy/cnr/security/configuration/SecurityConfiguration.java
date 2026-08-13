package com.toy.cnr.security.configuration;

import com.toy.cnr.security.filter.OAuthAuthenticationFilter;
import com.toy.cnr.security.filter.RefreshAuthenticationFilter;
import com.toy.cnr.security.filter.UserAuthenticationFilter;
import com.toy.cnr.security.filter.UserAuthorizationFilter;
import com.toy.cnr.security.provider.PermitMatcherProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final PermitMatcherProvider permitMatcherProvider;
    private final UserAuthenticationFilter userAuthenticationFilter;
    private final OAuthAuthenticationFilter oAuthAuthenticationFilter;
    private final RefreshAuthenticationFilter refreshAuthenticationFilter;
    private final UserAuthorizationFilter userAuthorizationFilter;

    public SecurityConfiguration(
        @Qualifier("permitMatcherProvider") PermitMatcherProvider permitMatcherProvider,
        @Qualifier("userAuthenticationFilter") UserAuthenticationFilter userAuthenticationFilter,
        @Qualifier("oAuthAuthenticationFilter") OAuthAuthenticationFilter oAuthAuthenticationFilter,
        @Qualifier("refreshAuthenticationFilter") RefreshAuthenticationFilter refreshAuthenticationFilter,
        @Qualifier("userAuthorizationFilter") UserAuthorizationFilter userAuthorizationFilter
    ) {
        this.permitMatcherProvider = permitMatcherProvider;
        this.userAuthenticationFilter = userAuthenticationFilter;
        this.oAuthAuthenticationFilter = oAuthAuthenticationFilter;
        this.refreshAuthenticationFilter = refreshAuthenticationFilter;
        this.userAuthorizationFilter = userAuthorizationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(permitMatcherProvider.getAsArray()).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(userAuthenticationFilter, LogoutFilter.class)
            .addFilterBefore(oAuthAuthenticationFilter, LogoutFilter.class)
            .addFilterBefore(refreshAuthenticationFilter, LogoutFilter.class)
            .addFilterBefore(userAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .build();
    }
}
