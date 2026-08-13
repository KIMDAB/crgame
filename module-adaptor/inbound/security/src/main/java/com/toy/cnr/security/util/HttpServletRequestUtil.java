package com.toy.cnr.security.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.util.Strings;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@UtilityClass
public class HttpServletRequestUtil {

    private static final String DEFAULT_NONE_MESSAGE = "Request-None";

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    public static Optional<Cookie> getCookie(HttpServletRequest request, String value) {
        try {
            return Arrays.stream(request.getCookies())
                .filter(cookie -> value.equals(cookie.getName()))
                .findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static List<String> getCookiesAsString() {
        try {
            final HttpServletRequest request = getRequest();
            return Arrays.stream(request.getCookies())
                .map(c -> c.getName() + ":" + c.getValue())
                .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static String getOnlyUrl(HttpServletRequest request) {
        return request.getRequestURI();
    }

    public static String getOnlyUrlWithQuery() {
        try {
            final HttpServletRequest request = getRequest();
            return getOnlyUrlWithQuery(request);
        } catch (Exception e) {
            return DEFAULT_NONE_MESSAGE;
        }
    }

    public static String getOnlyUrlWithQuery(HttpServletRequest request) {
        return getOnlyUrl(request) +
            Optional.ofNullable(request.getQueryString())
                .map(qs -> "?" + qs)
                .orElse(Strings.EMPTY);
    }

    public static void flushObject(
        HttpServletResponse response,
        int status,
        Object object
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        PrintWriter writer = response.getWriter();
        writer.println(MapperUtil.toJson(object));
        response.flushBuffer();
    }
}