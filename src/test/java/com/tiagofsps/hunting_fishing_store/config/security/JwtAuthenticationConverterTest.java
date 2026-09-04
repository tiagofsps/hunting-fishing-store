package com.tiagofsps.hunting_fishing_store.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;

import java.util.Collection;
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
}
