package com.tiagofsps.hunting_fishing_store.config.security;

import java.time.Instant;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final Duration tokenExpiration;
    private final Duration refreshTokenExpiration;

    public TokenService(
        JwtEncoder jwtEncoder,
        @Value("${app.jwt.expiration}") Duration tokenExpiration,
        @Value("${app.jwt.refresh-expiration}") Duration refreshTokenExpiration) {
        this.jwtEncoder = jwtEncoder;
        this.tokenExpiration = tokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(String username) {
        return generateToken(username, tokenExpiration, "access");
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, refreshTokenExpiration, "refresh");
    }

    private String generateToken(String username, Duration expiration, String tokenType) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(expiration))
                .subject(username)
                .claim("token_type", tokenType)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
