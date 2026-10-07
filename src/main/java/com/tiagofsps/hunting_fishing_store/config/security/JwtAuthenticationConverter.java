package com.tiagofsps.hunting_fishing_store.config.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String ROLES_CLAIM = "roles";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Validate token type - only accept access tokens
        String tokenType = jwt.getClaimAsString(TOKEN_TYPE_CLAIM);
        if (!ACCESS_TOKEN_TYPE.equals(tokenType)) {
            throw new JwtException("Invalid token type. Expected 'access', got: " + tokenType);
        }

        // Extract and convert roles to GrantedAuthorities
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    /**
     * Reads the {@code roles} claim from a decoded JWT.
     *
     * <p>The claim is written as a JSON array, but its Java type after decoding
     * depends on the parser: Spring's {@code NimbusJwtDecoder} goes through
     * nimbus' Gson-based {@code JSONObjectUtils.parse}, which materialises JSON
     * arrays as {@link java.util.List}. Handing a {@code List} to a branch that
     * only knows {@code String[]}/{@code String} silently downgrades every
     * caller (including admins) to {@code ROLE_USER}, so all
     * {@code @PreAuthorize("hasRole('ADMIN')")"} endpoints answer 403
     * "Access Denied". Every collection/array/string shape is therefore handled
     * explicitly, and an empty result falls back to the default role.
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Object roles = jwt.getClaim(ROLES_CLAIM);

        if (roles == null) {
            return defaultRole();
        }

        if (roles instanceof Collection<?> collection) {
            return toAuthorities(collection.stream()
                    .filter(Objects::nonNull)
                    .map(value -> value instanceof String s ? s : String.valueOf(value))
                    .toList());
        }

        if (roles instanceof String[] array) {
            return toAuthorities(Arrays.stream(array)
                    .filter(Objects::nonNull)
                    .toList());
        }

        if (roles instanceof String single) {
            return toAuthorities(Arrays.asList(single));
        }

        return defaultRole();
    }

    private Collection<GrantedAuthority> toAuthorities(Collection<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                .filter(role -> !role.isBlank())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return authorities.isEmpty() ? defaultRole() : authorities;
    }

    private Collection<GrantedAuthority> defaultRole() {
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
