package com.tiagofsps.hunting_fishing_store.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationConverterTest {

    private final JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    @Test
    void shouldConvertValidAccessToken() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("token_type")).thenReturn("access");
        when(jwt.getSubject()).thenReturn("user@example.com");
        when(jwt.getClaim("roles")).thenReturn(new String[]{"ROLE_USER", "ROLE_ADMIN"});

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication.getName()).isEqualTo("user@example.com");
        Collection<GrantedAuthority> authorities = authentication.getAuthorities();
        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void shouldRejectRefreshToken() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("token_type")).thenReturn("refresh");

        assertThatThrownBy(() -> converter.convert(jwt))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Invalid token type");
    }

    @Test
    void shouldHandleMissingRolesByAssigningDefaultUserRole() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("token_type")).thenReturn("access");
        when(jwt.getSubject()).thenReturn("user@example.com");
        when(jwt.getClaim("roles")).thenReturn(null);

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void shouldHandleSingleStringRole() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("token_type")).thenReturn("access");
        when(jwt.getSubject()).thenReturn("user@example.com");
        when(jwt.getClaim("roles")).thenReturn("ROLE_MANAGER");

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_MANAGER");
    }

    @Test
    void shouldHandleListRoleClaim() {
        // This is the shape the decoder really produces: the roles claim is
        // written as a JSON array and comes back from nimbus' Gson parser as a
        // List, never as String[].
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("token_type")).thenReturn("access");
        when(jwt.getSubject()).thenReturn("admin@example.com");
        when(jwt.getClaim("roles")).thenReturn(List.of("ROLE_ADMIN"));

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void shouldFallBackToDefaultRoleWhenRoleClaimIsEmptyOrBlank() {
        Jwt emptyRoles = mock(Jwt.class);
        when(emptyRoles.getClaimAsString("token_type")).thenReturn("access");
        when(emptyRoles.getSubject()).thenReturn("user@example.com");
        when(emptyRoles.getClaim("roles")).thenReturn(List.of());

        Jwt blankRole = mock(Jwt.class);
        when(blankRole.getClaimAsString("token_type")).thenReturn("access");
        when(blankRole.getSubject()).thenReturn("user@example.com");
        when(blankRole.getClaim("roles")).thenReturn("   ");

        assertThat(converter.convert(emptyRoles).getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        assertThat(converter.convert(blankRole).getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void shouldCarryAdminRoleThroughRealEncodeDecodePipeline() throws Exception {
        // Mirrors TokenService + SecurityConfig: HS256 secret, roles claim as a
        // JSON array, decoded by NimbusJwtDecoder. Guards the encoder/decoder/
        // converter contract that @PreAuthorize("hasRole('ADMIN')") depends on.
        byte[] secret = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);
        SecretKey key = new SecretKeySpec(secret, "HmacSHA256");

        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("admin@example.com")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .claim("token_type", "access")
                .claim("roles", List.of("ROLE_ADMIN"))
                .build();
        SignedJWT signedJwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(), claims);
        signedJwt.sign(new MACSigner(secret));

        Jwt decoded = NimbusJwtDecoder.withSecretKey(key).build().decode(signedJwt.serialize());

        Object decodedRolesClaim = decoded.getClaim("roles");
        assertThat(decodedRolesClaim).isInstanceOf(List.class);

        AbstractAuthenticationToken authentication = converter.convert(decoded);

        assertThat(authentication.getName()).isEqualTo("admin@example.com");
        assertThat(authentication.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }
}
