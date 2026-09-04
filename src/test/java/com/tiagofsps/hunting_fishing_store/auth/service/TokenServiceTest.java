package com.tiagofsps.hunting_fishing_store.auth.service;

import com.tiagofsps.hunting_fishing_store.auth.entity.RefreshToken;
import com.tiagofsps.hunting_fishing_store.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private TokenService tokenService;

    private final Duration accessExpiration = Duration.ofMinutes(15);
    private final Long refreshExpirationSeconds = 604800L;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(jwtEncoder, refreshTokenRepository, accessExpiration, refreshExpirationSeconds);
    }

    @Test
    void shouldGenerateAccessToken() {
        String username = "user@example.com";
        String role = "ROLE_USER";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("access-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        String token = tokenService.generateAccessToken(username, role);

        assertThat(token).isEqualTo("access-token");

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());

        JwtClaimsSet claims = captor.getValue().getClaims();
        assertThat(claims.getSubject()).isEqualTo(username);
        assertThat((String) claims.getClaim("token_type")).isEqualTo("access");
        assertThat((String[]) claims.getClaim("roles")).isEqualTo(new String[]{role});
    }

    @Test
    void shouldGenerateRefreshToken() {
        String username = "user@example.com";
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("refresh-token");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        String token = tokenService.generateRefreshToken(username);

        assertThat(token).isEqualTo("refresh-token");

        // Verify Redis persistence
        ArgumentCaptor<RefreshToken> redisTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(redisTokenCaptor.capture());
        RefreshToken savedToken = redisTokenCaptor.getValue();
        assertThat(savedToken.getUsername()).isEqualTo(username);
        assertThat(savedToken.getId()).isNotNull();

        // Verify JWT claims
        ArgumentCaptor<JwtEncoderParameters> jwtCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(jwtCaptor.capture());
        JwtClaimsSet claims = jwtCaptor.getValue().getClaims();
        assertThat(claims.getSubject()).isEqualTo(username);
        assertThat((String) claims.getClaim("token_type")).isEqualTo("refresh");
        assertThat(claims.getId()).isEqualTo(savedToken.getId());
    }

    @Test
    void shouldValidateRefreshToken() {
        String tokenId = "some-id";
        when(refreshTokenRepository.existsById(tokenId)).thenReturn(true);

        boolean isValid = tokenService.validateRefreshToken(tokenId);

        assertThat(isValid).isTrue();
        verify(refreshTokenRepository).existsById(tokenId);
    }

    @Test
    void shouldRevokeRefreshToken() {
        String tokenId = "some-id";

        tokenService.revokeRefreshToken(tokenId);

        verify(refreshTokenRepository).deleteById(tokenId);
    }
}
