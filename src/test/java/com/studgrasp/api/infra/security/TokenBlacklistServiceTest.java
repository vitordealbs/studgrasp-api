package com.studgrasp.api.infra.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private TokenBlacklistService tokenBlacklistService;

    // A minimal three-part JWT-like token: header.payload.signature
    private static final String TOKEN = "header.payload.mysignature";
    private static final String EXPECTED_KEY = "token_bl:mysignature";

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService(redisTemplate);
    }

    @Test
    void blacklist_shouldStoreKeyWithTtlWhenTtlIsPositive() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        tokenBlacklistService.blacklist(TOKEN, 60_000L);

        verify(valueOps).set(eq(EXPECTED_KEY), eq("1"), eq(60_000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    void blacklist_shouldNotStoreWhenTtlIsZero() {
        tokenBlacklistService.blacklist(TOKEN, 0L);

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void blacklist_shouldNotStoreWhenTtlIsNegative() {
        tokenBlacklistService.blacklist(TOKEN, -1L);

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void isBlacklisted_shouldReturnTrueWhenKeyExists() {
        when(redisTemplate.hasKey(EXPECTED_KEY)).thenReturn(Boolean.TRUE);

        assertThat(tokenBlacklistService.isBlacklisted(TOKEN)).isTrue();
    }

    @Test
    void isBlacklisted_shouldReturnFalseWhenKeyDoesNotExist() {
        when(redisTemplate.hasKey(EXPECTED_KEY)).thenReturn(Boolean.FALSE);

        assertThat(tokenBlacklistService.isBlacklisted(TOKEN)).isFalse();
    }

    @Test
    void isBlacklisted_shouldFailOpenWhenRedisThrows() {
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Redis unavailable"));

        // Fail-open: must return false instead of propagating exception
        assertThat(tokenBlacklistService.isBlacklisted(TOKEN)).isFalse();
    }

    @Test
    void blacklist_shouldFailSilentlyWhenRedisThrows() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        doThrow(new RuntimeException("Redis unavailable"))
                .when(valueOps).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        // Must not throw
        tokenBlacklistService.blacklist(TOKEN, 60_000L);
    }
}
