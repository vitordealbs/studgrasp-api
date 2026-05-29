package com.studgrasp.api.infra.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "token_bl:";

    private final StringRedisTemplate redisTemplate;

    /**
     * Blacklists the given JWT for the remainder of its validity window.
     *
     * @param token   raw JWT string
     * @param ttlMillis remaining milliseconds until the token would expire
     */
    public void blacklist(String token, long ttlMillis) {
        if (ttlMillis <= 0) {
            return; // already expired, no need to store
        }
        String key = KEY_PREFIX + extractSignature(token);
        try {
            redisTemplate.opsForValue().set(key, "1", ttlMillis, TimeUnit.MILLISECONDS);
            log.debug("[SECURITY] event=TOKEN_BLACKLISTED key={}", key);
        } catch (Exception e) {
            log.error("[SECURITY] event=BLACKLIST_WRITE_ERROR msg={}", e.getMessage());
        }
    }

    /**
     * Returns true if the given token has been blacklisted (i.e. the user has logged out).
     */
    public boolean isBlacklisted(String token) {
        String key = KEY_PREFIX + extractSignature(token);
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("[SECURITY] event=BLACKLIST_READ_ERROR msg={} — failing open", e.getMessage());
            return false; // fail-open: Redis down => allow traffic
        }
    }

    private String extractSignature(String token) {
        String[] parts = token.split("\\.");
        return parts[parts.length - 1];
    }
}
