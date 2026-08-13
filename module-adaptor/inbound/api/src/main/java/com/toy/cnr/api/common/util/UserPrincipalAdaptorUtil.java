package com.toy.cnr.api.common.util;

import com.toy.cnr.application.user.error.UserUnAuthenticatedException;
import com.toy.cnr.domain.user.User;
import com.toy.cnr.security.model.detail.AuthenticatedUser;
import com.toy.cnr.security.util.UserPrincipalUtil;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserPrincipalAdaptorUtil {

    public static User getUserInfo() {
        return UserPrincipalUtil.getAuthenticatedUser()
            .map(AuthenticatedUser::getUserInfo)
            .map(User.class::cast)
            .orElseThrow(UserUnAuthenticatedException::of);
    }
}
