package com.toy.cnr.security.util;

import com.toy.cnr.security.model.authentication.UserPrincipal;
import com.toy.cnr.security.model.detail.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

@UtilityClass
public class UserPrincipalUtil {

    public static UserPrincipal getUserPrincipal() {
        final var request = HttpServletRequestUtil.getRequest();
        return Optional.of(request)
            .map(HttpServletRequest::getUserPrincipal)
            .filter(UserPrincipal.class::isInstance)
            .map(UserPrincipal.class::cast)
            .orElseThrow(() -> new InternalAuthenticationServiceException("잘못된 principal 입니다."));
    }

    public static User getUser() {
        return (User) getUserPrincipal().getUser();
    }

    public static Optional<AuthenticatedUser> getAuthenticatedUser() {
        final var user = getUserPrincipal().getUser();
        return user instanceof AuthenticatedUser
            ? Optional.of((AuthenticatedUser) user)
            : Optional.empty();
    }
}
