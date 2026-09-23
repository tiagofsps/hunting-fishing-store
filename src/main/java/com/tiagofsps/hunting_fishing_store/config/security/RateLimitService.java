package com.tiagofsps.hunting_fishing_store.config.security;

import com.tiagofsps.hunting_fishing_store.common.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-backed sliding-window rate limiting. Each attempt is recorded in a
 * sorted set keyed by scope; attempts older than the configured window are
 * pruned and the request is refused once the count exceeds the limit.
 */
@Service
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final int maxAttempts;
    private final long windowSeconds;

    public RateLimitService(
            StringRedisTemplate redisTemplate,
            @Value("${app.rate-limit.max-attempts:5}") int maxAttempts,
            @Value("${app.rate-limit.window-seconds:900}") long windowSeconds) {
        this.redisTemplate = redisTemplate;
        this.maxAttempts = maxAttempts;
        this.windowSeconds = windowSeconds;
    }

    /**
     * Records an attempt and returns whether it is within the allowed limit.
     */
    public boolean isAllowed(String identifier) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowSeconds * 1000L;

        String redisKey = key(identifier);
        var zset = redisTemplate.opsForZSet();
        zset.removeRangeByScore(redisKey, 0, windowStart);
        zset.add(redisKey, String.valueOf(now), now);
        redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));

        Long count = zset.zCard(redisKey);
        return count == null || count <= maxAttempts;
    }

    /**
     * Records an attempt and throws when the limit is exceeded.
     */
    public void check(String identifier) {
        if (!isAllowed(identifier)) {
            throw new RateLimitExceededException(windowSeconds);
        }
    }

    private String key(String identifier) {
        return "rate-limit:" + identifier;
    }
}