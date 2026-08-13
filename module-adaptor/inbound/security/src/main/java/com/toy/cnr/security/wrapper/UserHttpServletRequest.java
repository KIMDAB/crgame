package com.toy.cnr.security.wrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.security.core.Authentication;

import java.security.Principal;

public final class UserHttpServletRequest extends HttpServletRequestWrapper {

    private final Authentication authenticated;

    public UserHttpServletRequest(HttpServletRequest request, Authentication authenticated) {
        super(request);
        this.authenticated = authenticated;
    }

    @Override
    public Principal getUserPrincipal() {
        return authenticated;
    }
}