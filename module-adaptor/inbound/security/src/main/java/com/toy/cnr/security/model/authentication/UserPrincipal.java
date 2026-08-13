package com.toy.cnr.security.model.authentication;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class UserPrincipal implements Authentication {

    private final UserDetails user;
    private boolean authenticated;

    private UserPrincipal(UserDetails user, boolean authenticated) {
        this.user = user;
        this.authenticated = authenticated;
    }

    public static UserPrincipal authenticated(UserDetails user) {
        return new UserPrincipal(user, true);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return user.getPassword();
    }

    @Override
    public Object getDetails() {
        return user;
    }

    @Override
    public Object getPrincipal() {
        return user.getUsername();
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
            "userName=" + user.getUsername() +
            ", authorities=" + user.getAuthorities() +
            ", authenticated=" + authenticated +
            '}';
    }
}
