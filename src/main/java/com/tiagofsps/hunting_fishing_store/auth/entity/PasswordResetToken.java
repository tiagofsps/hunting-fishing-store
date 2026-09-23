package com.tiagofsps.hunting_fishing_store.auth.entity;

import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash(value = "password_reset_tokens")
public class PasswordResetToken implements Serializable {

    @Id
    private String token;

    private String email;

    @TimeToLive
    private Long ttlInSeconds;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, String email, Long ttlInSeconds) {
        this.token = token;
        this.email = email;
        this.ttlInSeconds = ttlInSeconds;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getTtlInSeconds() {
        return ttlInSeconds;
    }

    public void setTtlInSeconds(Long ttlInSeconds) {
        this.ttlInSeconds = ttlInSeconds;
    }
}