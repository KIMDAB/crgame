package com.toy.cnr.application.user.service;

import com.toy.cnr.domain.common.CommandResult;
import com.toy.cnr.domain.user.User;
import com.toy.cnr.port.common.RepositoryResult;
import com.toy.cnr.port.oauth.OAuthProviderRepository;
import com.toy.cnr.port.user.UserAuthOAuthRepository;
import com.toy.cnr.port.user.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class OAuthUserService {

    private final OAuthProviderRepository oAuthProviderRepository;
    private final UserService userService;
    private final UserAuthOAuthRepository userAuthOAuthRepository;
    private final UserRepository userRepository;

    public OAuthUserService(
        OAuthProviderRepository oAuthProviderRepository,
        UserService userService,
        UserAuthOAuthRepository userAuthOAuthRepository,
        UserRepository userRepository
    ) {
        this.oAuthProviderRepository = oAuthProviderRepository;
        this.userService = userService;
        this.userAuthOAuthRepository = userAuthOAuthRepository;
        this.userRepository = userRepository;
    }

    public CommandResult<User> findByOAuthToken(String provider, String accessToken) {
        return switch (oAuthProviderRepository.fetchUserInfo(provider, accessToken)) {
            case RepositoryResult.Found(var info) -> switch (userAuthOAuthRepository.findByProviderAndOauthId(provider, info.oauthId())) {
                case RepositoryResult.Found(var ignored) -> switch (userRepository.findByEmail(info.email())) {
                    case RepositoryResult.Found(var userDto) -> new CommandResult.Success<>(
                        new User(userDto.id(), userDto.email(), userDto.name(), userDto.nickname(), userDto.createdAt()), null
                    );
                    case RepositoryResult.NotFound(var msg) -> new CommandResult.BusinessError<>(msg);
                    case RepositoryResult.Error(var t) -> new CommandResult.BusinessError<>(t.getMessage());
                };
                case RepositoryResult.NotFound(var ignored) -> new CommandResult.BusinessError<>("가입되지 않은 사용자입니다.");
                case RepositoryResult.Error(var t) -> new CommandResult.BusinessError<>(t.getMessage());
            };
            case RepositoryResult.NotFound(var msg) -> new CommandResult.BusinessError<>(msg);
            case RepositoryResult.Error(var t) -> new CommandResult.BusinessError<>(t.getMessage());
        };
    }

    public CommandResult<User> createByOAuthToken(String provider, String accessToken) {
        return switch (oAuthProviderRepository.fetchUserInfo(provider, accessToken)) {
            case RepositoryResult.Found(var info) -> switch (userAuthOAuthRepository.findByProviderAndOauthId(provider, info.oauthId())) {
                case RepositoryResult.Found(var ignored) -> new CommandResult.BusinessError<>("이미 가입된 사용자입니다.");
                case RepositoryResult.NotFound(var ignored) -> userService.createByOAuth(
                    provider,
                    info.oauthId(),
                    info.email(),
                    info.name(),
                    accessToken
                );
                case RepositoryResult.Error(var t) -> new CommandResult.BusinessError<>(t.getMessage());
            };
            case RepositoryResult.NotFound(var msg) -> new CommandResult.BusinessError<>(msg);
            case RepositoryResult.Error(var t) -> new CommandResult.BusinessError<>(t.getMessage());
        };
    }

    /**
     * OAuth 회원가입/로그인 — 미가입 시 가입, 이미 가입된 계정이면 로그인 처리.
     */
    public CommandResult<User> registerOrSignInByOAuthToken(String provider, String accessToken) {
        return switch (oAuthProviderRepository.fetchUserInfo(provider, accessToken)) {
            case RepositoryResult.Found(var info) -> switch (userAuthOAuthRepository.findByProviderAndOauthId(provider, info.oauthId())) {
                case RepositoryResult.Found(var ignored) -> findUserByEmail(info.email());
                case RepositoryResult.NotFound(var ignored) -> userService.createByOAuth(
                    provider,
                    info.oauthId(),
                    info.email(),
                    info.name(),
                    accessToken
                );
                case RepositoryResult.Error(var t) -> new CommandResult.BusinessError<>(t.getMessage());
            };
            case RepositoryResult.NotFound(var msg) -> new CommandResult.BusinessError<>(msg);
            case RepositoryResult.Error(var t) -> new CommandResult.BusinessError<>(t.getMessage());
        };
    }

    private CommandResult<User> findUserByEmail(String email) {
        return switch (userRepository.findByEmail(email)) {
            case RepositoryResult.Found(var userDto) -> new CommandResult.Success<>(
                new User(userDto.id(), userDto.email(), userDto.name(), userDto.nickname(), userDto.createdAt()), null
            );
            case RepositoryResult.NotFound(var msg) -> new CommandResult.BusinessError<>(msg);
            case RepositoryResult.Error(var t) -> new CommandResult.BusinessError<>(t.getMessage());
        };
    }
}
