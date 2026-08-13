package com.toy.cnr.security.model.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@ToString
public class UnAuthentication implements Authentication {

    @JsonIgnore
    private static final DisabledException UNUSED_EXCEPTION = new DisabledException("unused");

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        throw UNUSED_EXCEPTION;
    }

    @Override
    @JsonIgnore
    public Object getCredentials() {
        throw UNUSED_EXCEPTION;
    }

    @Override
    @JsonIgnore
    public Object getDetails() {
        throw UNUSED_EXCEPTION;
    }

    @Override
    @JsonIgnore
    public Object getPrincipal() {
        throw UNUSED_EXCEPTION;
    }

    @Override
    @JsonIgnore
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    @JsonIgnore
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw UNUSED_EXCEPTION;
    }

    @Override
    @JsonIgnore
    public String getName() {
        throw UNUSED_EXCEPTION;
    }
}
