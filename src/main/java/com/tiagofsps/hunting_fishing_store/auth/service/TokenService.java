package com.tiagofsps.hunting_fishing_store.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.tiagofsps.hunting_fishing_store.auth.entity.RefreshToken;
import com.tiagofsps.hunting_fishing_store.auth.repository.RefreshTokenRepository;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Duration accessExpiration;
    private final Long refreshExpirationSeconds;

    public TokenService(
            JwtEncoder jwtEncoder,
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.jwt.access-expiration}") Duration accessExpiration,
            @Value("${app.jwt.refresh-expiration-seconds}") Long refreshExpirationSeconds
    ) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.accessExpiration = accessExpiration;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    public String generateAccessToken(String username) {
        return generateAccessToken(username, "ROLE_USER");
    }

    public String generateAccessToken(String username, String role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(accessExpiration))
                .subject(username)
                .claim("token_type", "access")
                .claim("roles", new String[]{role})
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(String username) {
        return generateAndSaveRefreshToken(username);
    }

    public String generateAndSaveRefreshToken(String username) {
        String tokenId = UUID.randomUUID().toString();

        // 1. Save stateful token entry in Redis with TTL
        RefreshToken redisToken = new RefreshToken(tokenId, username, refreshExpirationSeconds);
        refreshTokenRepository.save(redisToken);

        // 2. Encode JWT with matching token ID in claims
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(refreshExpirationSeconds))
                .subject(username)
                .claim("token_type", "refresh")
                .id(tokenId)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public boolean validateRefreshToken(String tokenId) {
        // Check if token exists in Redis (if deleted/revoked, returns false)
        return refreshTokenRepository.existsById(tokenId);
    }

    public void revokeRefreshToken(String tokenId) {
        // Instant revocation on logout
        refreshTokenRepository.deleteById(tokenId);
    }

    public Long getRefreshExpirationSeconds() {
        return refreshExpirationSeconds;
    }
}
