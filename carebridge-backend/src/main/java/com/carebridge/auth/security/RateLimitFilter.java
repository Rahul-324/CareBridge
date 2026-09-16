package com.carebridge.auth.security;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final SecurityErrorWriter securityErrorWriter;
    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    @Value("${rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${rate-limit.login.max-attempts:5}")
    private int loginMaxAttempts;

    @Value("${rate-limit.login.window-ms:60000}")
    private long loginWindowMs;

    @Value("${rate-limit.onboarding.max-attempts:10}")
    private int onboardingMaxAttempts;

    @Value("${rate-limit.onboarding.window-ms:3600000}")
    private long onboardingWindowMs;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!enabled || !HttpMethod.POST.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        Limit limit = limitFor(request.getRequestURI());
        if (limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = limit.name() + ":" + clientIp(request);
        if (!tryAcquire(key, limit.maxAttempts(), limit.windowMs())) {
            securityErrorWriter.write(
                    response,
                    HttpStatus.TOO_MANY_REQUESTS,
                    "too many requests"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Limit limitFor(String uri) {
        if ("/api/v1/auth/login".equals(uri)
                || "/api/v1/auth/refresh".equals(uri)
                || "/api/v1/auth/logout".equals(uri)
                || "/api/v1/auth/accept-invite".equals(uri)
                || "/api/v1/staff/invites".equals(uri)) {
            return new Limit("auth", loginMaxAttempts, loginWindowMs);
        }
        if (uri != null && uri.startsWith("/api/v1/onboarding")) {
            return new Limit("onboarding", onboardingMaxAttempts, onboardingWindowMs);
        }
        return null;
    }

    private boolean tryAcquire(String key, int maxAttempts, long windowMs) {
        long now = System.currentTimeMillis();
        long cutoff = now - windowMs;
        Deque<Long> timestamps = hits.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= maxAttempts) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record Limit(String name, int maxAttempts, long windowMs) {
    }
}
