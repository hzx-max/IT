package com.netconfig.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
@Order(1)
public class RateLimitingFilter implements Filter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 5 * 60 * 1000L;

    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Long>> attempts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        if (path != null && path.contains("/api/auth/login")) {
            String ip = getClientIp(req);
            if (isRateLimited(ip)) {
                res.setStatus(429);
                res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"ok\":false,\"error\":\"登录尝试过于频繁，请5分钟后再试\"}");
                return;
            }
            chain.doFilter(request, response);
            // 只在登录失败后记录次数（状态码401表示认证失败）
            if (res.getStatus() == 401) {
                recordAttempt(ip);
            }
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isRateLimited(String ip) {
        ConcurrentLinkedDeque<Long> timestamps = attempts.get(ip);
        if (timestamps == null) return false;
        long now = System.currentTimeMillis();
        long cutoff = now - WINDOW_MS;
        while (!timestamps.isEmpty() && timestamps.peekFirst() < cutoff) {
            timestamps.pollFirst();
        }
        return timestamps.size() >= MAX_ATTEMPTS;
    }

    private void recordAttempt(String ip) {
        attempts.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>()).addLast(System.currentTimeMillis());
    }
}