package com.toy.cnr.api.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.pattern:/**}")
    private String pattern;

    @Value("#{'${cors.allowedOrigins:}'.split(' ')}")
    private List<String> allowedOrigins;

    @Value("#{'${cors.allowedHeaders:}'.split(' ')}")
    private List<String> allowedHeaders;

    @Value("#{'${cors.exposedHeaders:}'.split(' ')}")
    private List<String> exposedHeaders;

    @Value("#{'${cors.allowedMethods:}'.split(' ')}")
    private List<String> allowedMethods;

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedHeaders(allowedHeaders);
        config.setExposedHeaders(exposedHeaders);
        config.setAllowedMethods(allowedMethods);
        source.registerCorsConfiguration(pattern, config);
        return new CorsFilter(source);
    }
}
