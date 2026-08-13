package com.toy.cnr.security.model.detail;

import com.toy.cnr.security.exception.InvalidPasswordException;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serializable;
import java.util.Collection;

@Getter
@ToString
public class AuthenticatedUser extends User implements Serializable {

    private static final String ACCOUNT_EXPIRED_MESSAGE = "계정이 만료되었습니다.";
    private static final String ACCOUNT_LOCKED_MESSAGE = "계정이 잠김 상태입니다.";
    private static final String PASSWORD_EXPIRED_MESSAGE = "패스워드 기한이 만료되었습니다.";
    private static final String ACCOUNT_DISABLED_MESSAGE = "계정이 비활성화 상태입니다.";

    public final transient Object userInfo;

    protected AuthenticatedUser(
        String username,
        String password,
        boolean enabled,
        boolean credentialsNonExpired,
        boolean accountNonLocked,
        Collection<? extends GrantedAuthority> authorities,
        Object userInfo
    ) {
        super(username, password, enabled, true, credentialsNonExpired, accountNonLocked, authorities);
        this.userInfo = userInfo;
    }

    public static AuthenticatedUser of(
        String username,
        String password,
        boolean enabled,
        boolean credentialsNonExpired,
        boolean accountNonLocked,
        Collection<? extends GrantedAuthority> authorities,
        Object userInfo
    ) {
        return new AuthenticatedUser(username, password, enabled, credentialsNonExpired, accountNonLocked, authorities, userInfo);
    }

    public void validateAccountStatus() throws AuthenticationException {
        if (!isAccountNonExpired()) {
            throw new AccountExpiredException(ACCOUNT_EXPIRED_MESSAGE);
        }
        if (!isAccountNonLocked()) {
            throw new LockedException(ACCOUNT_LOCKED_MESSAGE);
        }
        if (!isEnabled()) {
            throw new DisabledException(ACCOUNT_DISABLED_MESSAGE);
        }
    }

    public void validatePassword(
        PasswordEncoder passwordEncoder,
        String rawPassword
    ) throws AuthenticationException {
        if (!passwordEncoder.matches(rawPassword, getPassword())) {
            throw InvalidPasswordException.of();
        }
    }
}
