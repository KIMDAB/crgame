package com.toy.cnr.security.provider;

import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Setter
@Component("permitMatcherProvider")
@ConfigurationProperties("permit-matcher")
public class PermitMatcherProvider {

    private List<PermitMatcher> path;

    @ToString
    @Setter
    private static class PermitMatcher {
        private String method;
        private String pattern;

        public AntPathRequestMatcher pathMatcher() {
            return new AntPathRequestMatcher(
                pattern,
                "*".equals(method) ? null : method.toUpperCase(Locale.ROOT)
            );
        }
    }

    public RequestMatcher[] getAsArray() {
        return path.stream()
            .map(PermitMatcher::pathMatcher)
            .toArray(RequestMatcher[]::new);
    }

    public List<RequestMatcher> getAsList() {
        return path.stream()
            .map(PermitMatcher::pathMatcher)
            .collect(Collectors.toList());
    }
}
