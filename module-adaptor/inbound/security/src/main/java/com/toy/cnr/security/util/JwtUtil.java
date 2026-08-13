package com.toy.cnr.security.util;

import com.toy.cnr.security.model.jwt.JwtType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import static java.util.Objects.isNull;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.private}")
    private String jwtPrivateKey; // NOSONAR

    @Value("${jwt.accessToken.ttl}")
    private String jwtAccessTokenTtl; // NOSONAR

    @Value("${jwt.refreshToken.ttl}")
    private String jwtRefreshTokenTtl; // NOSONAR

    private static String jwtPrivateKeyStatic;
    private static Duration jwtAccessTokenTtlStatic;
    private static Duration jwtRefreshTokenTtlStatic;
    private static final String JWT_CLAIMS_TYPE_KEY = "type";

    @Value("${jwt.private}")
    private void setJwtPrivateKeyStatic(String jwtPrivateKey) {
        JwtUtil.jwtPrivateKeyStatic = jwtPrivateKey; // NOSONAR
    }

    @Value("${jwt.accessToken.ttl}")
    private void setJwtAccessTokenTtl(String jwtAccessTokenTtl) {
        JwtUtil.jwtAccessTokenTtlStatic = Duration.of(Long.parseLong(jwtAccessTokenTtl), ChronoUnit.SECONDS); // NOSONAR
    }

    @Value("${jwt.refreshToken.ttl}")
    private void setJwtRefreshTokenTtl(String jwtRefreshTokenTtl) {
        JwtUtil.jwtRefreshTokenTtlStatic = Duration.of(Long.parseLong(jwtRefreshTokenTtl), ChronoUnit.SECONDS); // NOSONAR
    }

    public static JwtToken issueAccessToken(String username) {
        return issueToken(username, jwtAccessTokenTtlStatic, JwtType.ACCESS_TOKEN);
    }

    public static JwtToken issueRefreshToken(String username) {
        return issueToken(username, jwtRefreshTokenTtlStatic, JwtType.REFRESH_TOKEN);
    }

    public static boolean isValid(String jwtToken) {
        try {
            if (StringUtils.isBlank(jwtToken)) {
                return false;
            }
            final var now = new Date();
            return parseClaims(jwtToken)
                .getExpiration()
                .after(now);
        } catch (ExpiredJwtException expired) {
            throw expired;
        } catch (JwtException e) {
            log.error("JwtUtil isValid error:{}", e.getMessage());
            return false;
        }
    }

    public static Optional<String> parseAccessToken(String jwtToken) {
        return parseToken(jwtToken, JwtType.ACCESS_TOKEN);
    }

    public static Optional<String> parseRefreshToken(String jwtToken) {
        return parseToken(jwtToken, JwtType.REFRESH_TOKEN);
    }

    public static Optional<LocalDateTime> parseExpiration(String jwtToken) {
        try {
             final var claims = parseClaims(jwtToken);
            return Optional.ofNullable(claims.getExpiration())
                .map(JwtUtil::toLocalDateTime);
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    private static Optional<String> parseToken(
        String jwtToken,
        JwtType jwtType
    ) {
        try {
            final var claims = parseClaims(jwtToken);
            validateJwtType(claims, jwtType);
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    private static Claims parseClaims(String jwtToken) throws JwtException {
        return Jwts.parser()
            .verifyWith(getSecretKey())
            .build()
            .parseSignedClaims(jwtToken)
            .getPayload();
    }

    private static JwtToken issueToken(
        String subject,
        Duration ttl,
        JwtType type
    ) {
        final var now = LocalDateTime.now();
        final var ttlFromNow = now.plus(ttl);
        final var token = Jwts.builder()
            .subject(subject)
            .issuedAt(toDate(now))
            .expiration(toDate(ttlFromNow))
            .claim(JWT_CLAIMS_TYPE_KEY, type.toString())
            .signWith(getSecretKey())
            .compact();
        return JwtToken.of(token, type, ttlFromNow);
    }

    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtPrivateKeyStatic.getBytes(StandardCharsets.UTF_8));
    }

    private static void validateJwtType(
        Claims claims,
        JwtType jwtType
    ) {
        final var rawClaimsJwtType = claims.get(JWT_CLAIMS_TYPE_KEY, String.class);
        if (isNull(rawClaimsJwtType) || !JwtType.valueOf(rawClaimsJwtType).equals(jwtType)) {
            throw new JwtException("invalid jwt type");
        }
    }

    private static Date toDate(LocalDateTime ldt) {
        final var instant = ldt.atZone(ZoneId.of("Asia/Seoul")).toInstant();
        return Date.from(instant);
    }

    private static LocalDateTime toLocalDateTime(Date dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert.getTime())
            .atZone(ZoneId.of("Asia/Seoul"))
            .toLocalDateTime();
    }

    @Getter
    public static class JwtToken {
        private final String token;
        private final JwtType jwtType;
        private final LocalDateTime expiresIn;

        private JwtToken(
            String token,
            JwtType jwtType,
            LocalDateTime expiresIn
        ) {
            this.token = token;
            this.jwtType = jwtType;
            this.expiresIn = expiresIn;
        }

        public static JwtToken of(
            String token,
            JwtType jwtType,
            LocalDateTime expiresIn
        ) {
            return new JwtToken(token, jwtType, expiresIn);
        }
    }
}
