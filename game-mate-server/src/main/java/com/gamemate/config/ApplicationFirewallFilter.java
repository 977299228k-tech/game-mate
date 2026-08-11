package com.gamemate.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 第二层应用防火墙。第一层 Nginx 被绕过时，后端仍会限制危险方法、
 * 扫描路径、超大请求、接口洪泛和登录爆破。
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ApplicationFirewallFilter extends OncePerRequestFilter {

    private static final long GENERAL_WINDOW_MILLIS = 60_000L;
    private static final long AUTH_WINDOW_MILLIS = 300_000L;
    private static final Set<String> ALLOWED_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD");
    private static final String[] BLOCKED_PATH_MARKERS = {
            "/.git", "/.env", "/actuator", "/swagger", "/v3/api-docs",
            "/wp-admin", "/phpmyadmin", "../", "..\\", "%2e%2e", "%00"
    };

    private final boolean enabled;
    private final int generalRequestsPerMinute;
    private final int authAttemptsPerFiveMinutes;
    private final long maxJsonBytes;
    private final long maxUploadBytes;
    private final ConcurrentHashMap<String, RateWindow> generalWindows = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateWindow> authWindows = new ConcurrentHashMap<>();
    private final AtomicLong requestCounter = new AtomicLong();

    public ApplicationFirewallFilter(
            @Value("${game-mate.firewall.enabled:true}") boolean enabled,
            @Value("${game-mate.firewall.general-requests-per-minute:240}") int generalRequestsPerMinute,
            @Value("${game-mate.firewall.auth-attempts-per-five-minutes:12}") int authAttemptsPerFiveMinutes,
            @Value("${game-mate.firewall.max-json-bytes:4194304}") long maxJsonBytes,
            @Value("${game-mate.firewall.max-upload-bytes:115343360}") long maxUploadBytes) {
        this.enabled = enabled;
        this.generalRequestsPerMinute = Math.max(1, generalRequestsPerMinute);
        this.authAttemptsPerFiveMinutes = Math.max(1, authAttemptsPerFiveMinutes);
        this.maxJsonBytes = Math.max(1024, maxJsonBytes);
        this.maxUploadBytes = Math.max(this.maxJsonBytes, maxUploadBytes);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        addSecurityHeaders(response);
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String method = request.getMethod().toUpperCase(Locale.ROOT);
        String path = request.getRequestURI();
        String clientIp = resolveClientIp(request);

        if (!ALLOWED_METHODS.contains(method)) {
            block(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "请求方法已被防火墙拒绝", 0);
            logBlocked(clientIp, method, path, "method");
            return;
        }

        if (isSuspiciousPath(path)) {
            block(response, HttpServletResponse.SC_NOT_FOUND, "请求地址不存在", 0);
            logBlocked(clientIp, method, path, "scanner-path");
            return;
        }

        long contentLength = request.getContentLengthLong();
        long contentLimit = isMultipartRequest(request) ? maxUploadBytes : maxJsonBytes;
        if (contentLength > contentLimit) {
            block(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "请求内容超过安全限制", 0);
            logBlocked(clientIp, method, path, "body-size");
            return;
        }

        long now = System.currentTimeMillis();
        if (!"OPTIONS".equals(method)
                && !allow(generalWindows, clientIp, generalRequestsPerMinute, GENERAL_WINDOW_MILLIS, now)) {
            block(response, 429, "请求过于频繁，请稍后再试", 60);
            logBlocked(clientIp, method, path, "general-rate");
            return;
        }

        if (isAuthenticationPath(path)
                && !allow(authWindows, clientIp, authAttemptsPerFiveMinutes, AUTH_WINDOW_MILLIS, now)) {
            block(response, 429, "登录或注册尝试过多，请五分钟后再试", 300);
            logBlocked(clientIp, method, path, "auth-rate");
            return;
        }

        cleanupExpiredWindows(now);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }

    private boolean allow(ConcurrentHashMap<String, RateWindow> windows,
                          String key,
                          int limit,
                          long windowMillis,
                          long now) {
        return windows.computeIfAbsent(key, ignored -> new RateWindow(now))
                .tryAcquire(limit, windowMillis, now);
    }

    private void cleanupExpiredWindows(long now) {
        if (requestCounter.incrementAndGet() % 1024 != 0) {
            return;
        }
        generalWindows.entrySet().removeIf(entry -> entry.getValue().isExpired(GENERAL_WINDOW_MILLIS * 2, now));
        authWindows.entrySet().removeIf(entry -> entry.getValue().isExpired(AUTH_WINDOW_MILLIS * 2, now));
    }

    private boolean isAuthenticationPath(String path) {
        return "/api/user/login".equals(path) || "/api/user/register".equals(path);
    }

    private boolean isSuspiciousPath(String path) {
        String normalized = path.toLowerCase(Locale.ROOT);
        for (String marker : BLOCKED_PATH_MARKERS) {
            if (normalized.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/form-data");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String address = request.getHeader("X-Real-IP");
        if (address == null || address.isBlank()) {
            address = request.getRemoteAddr();
        }
        int comma = address.indexOf(',');
        if (comma >= 0) {
            address = address.substring(0, comma);
        }
        address = address.trim();
        return address.length() <= 64 ? address : request.getRemoteAddr();
    }

    private void addSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Cache-Control", "no-store");
    }

    private void block(HttpServletResponse response, int status, String message, int retryAfterSeconds) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        if (retryAfterSeconds > 0) {
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        }
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\",\"data\":null}");
    }

    private void logBlocked(String clientIp, String method, String path, String reason) {
        log.warn("Application firewall blocked request: ip={}, method={}, path={}, reason={}",
                clientIp, method, path, reason);
    }

    private static final class RateWindow {
        private long startedAt;
        private int count;

        private RateWindow(long startedAt) {
            this.startedAt = startedAt;
        }

        private synchronized boolean tryAcquire(int limit, long windowMillis, long now) {
            if (now - startedAt >= windowMillis) {
                startedAt = now;
                count = 0;
            }
            if (count >= limit) {
                return false;
            }
            count++;
            return true;
        }

        private synchronized boolean isExpired(long expirationMillis, long now) {
            return now - startedAt >= expirationMillis;
        }
    }
}
