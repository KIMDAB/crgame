package com.toy.cnr.external.naver.map.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NaverMapProperties.class)
public class NaverMapConfiguration {
}
