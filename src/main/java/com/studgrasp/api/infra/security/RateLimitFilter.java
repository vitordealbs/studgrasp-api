package com.studgrasp.api.infra.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studgrasp.api.infra.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int WINDOW_SECONDS = 60;
    private static final int AUTH_LIMIT = 10;
    private static final int DEFAULT_LIMIT = 200;
    private static final String KEY_PREFIX = "rl:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ip = getClientIp(request);
        String path = request.getRequestURI();
        boolean isAuthPath = path.startsWith("/api/v1/auth/");
        int limit = isAuthPath ? AUTH_LIMIT : DEFAULT_LIMIT;
        String bucketKey = KEY_PREFIX + (isAuthPath ? "auth:" : "gen:") + ip;

        try {
            Long count = redisTemplate.opsForValue().increment(bucketKey);
            if (count != null && count == 1) {
                redisTemplate.expire(bucketKey, WINDOW_SECONDS, TimeUnit.SECONDS);
            }
            if (count != null && count > limit) {
                log.warn("[SECURITY] event=RATE_LIMIT_EXCEEDED ip={} path={} count={} limit={}", ip, path, count, limit);
                sendTooManyRequests(response);
                return;
            }
        } catch (Exception e) {
            // Fail-open: if Redis is unavailable, allow the request through
            log.error("[SECURITY] event=RATE_LIMIT_REDIS_ERROR ip={} path={} msg={} — failing open", ip, path, e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void sendTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(WINDOW_SECONDS));
        ErrorResponse body = new ErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too many requests. Please retry after " + WINDOW_SECONDS + " seconds.",
                null
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
