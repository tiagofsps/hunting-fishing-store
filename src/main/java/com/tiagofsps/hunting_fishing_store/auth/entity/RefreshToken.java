package com.tiagofsps.hunting_fishing_store.auth.entity;

import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash(value = "refresh_tokens")
public class RefreshToken implements Serializable {

    @Id
    private String id; // Generated UUID or random string

    @Indexed
    private String username; // Allows querying by username if needed

    @TimeToLive
    private Long ttlInSeconds; // Redis will purge this entry automatically when timer hits 0

    public RefreshToken() {}

    public RefreshToken(String id, String username, Long ttlInSeconds) {
        this.id = id;
        this.username = username;
        this.ttlInSeconds = ttlInSeconds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getTtlInSeconds() {
        return ttlInSeconds;
    }

    public void setTtlInSeconds(Long ttlInSeconds) {
        this.ttlInSeconds = ttlInSeconds;
    }
}