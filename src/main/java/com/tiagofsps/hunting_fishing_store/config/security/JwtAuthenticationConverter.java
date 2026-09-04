package com.tiagofsps.hunting_fishing_store.config.security;

import java.util.Arrays;
import java.util.Collection;
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

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Object roles = jwt.getClaim(ROLES_CLAIM);

        if (roles == null) {
            return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        if (roles instanceof String[]) {
            return Arrays.stream((String[]) roles)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        if (roles instanceof String) {
            return Arrays.asList(new SimpleGrantedAuthority((String) roles));
        }

        return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
