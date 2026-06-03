package com.netconfig.config;

import com.netconfig.entity.UserToken;
import com.netconfig.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 公共接口放行
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register")) {
            return true;
        }

        // 点击记录接口公开（所有用户可记录点击）
        if (path.startsWith("/api/clicks")) {
            return true;
        }

        // ADMIN提交待审核变更的接口（需要auth但不需要SUPER_ADMIN）
        if (path.startsWith("/api/admin/pending-change") && "POST".equalsIgnoreCase(method)) {
            return checkAuthWithoutSuperAdmin(request, response);
        }

        // 待审核变更管理接口（仅SUPER_ADMIN）
        if (path.startsWith("/api/admin/pending-changes") || path.startsWith("/api/admin/pending-change/")) {
            return checkSuperAdminOnly(request, response);
        }

        // 写操作需要管理员权限
        boolean isWriteOperation = "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);

        // 读操作对所有人放行（包括/admin路由）
        // admin页面在前端由路由守卫控制，普通用户看不到管理页面链接
        if (!isWriteOperation) {
            return true;
        }

        // 文件上传接口 - 允许ADMIN和SUPER_ADMIN（用于审核流程中上传图片）
        if (path.startsWith("/api/upload")) {
            return checkAuthWithoutSuperAdmin(request, response);
        }

        // 写操作需要管理员鉴权
        return checkAuth(request, response);
    }

    private boolean checkAuth(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\"}");
            return false;
        }
        UserToken token = authService.validateToken(auth.substring(7));
        if (token == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"登录已过期，请重新登录\"}");
            return false;
        }
        // 只有SUPER_ADMIN可以写操作，ADMIN需要通过待审核变更流程
        if (!"SUPER_ADMIN".equals(token.getRole())) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"msg\":\"普通管理员需通过超级管理员审核后才能操作数据，请通过审核流程提交变更\"}");
            return false;
        }
        // 存储用户信息到request
        request.setAttribute("userId", token.getUserId());
        request.setAttribute("userRole", token.getRole());
        return true;
    }

    // 允许ADMIN和SUPER_ADMIN访问（用于提交待审核变更）
    private boolean checkAuthWithoutSuperAdmin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\"}");
            return false;
        }
        UserToken token = authService.validateToken(auth.substring(7));
        if (token == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"登录已过期，请重新登录\"}");
            return false;
        }
        if (!"SUPER_ADMIN".equals(token.getRole()) && !"ADMIN".equals(token.getRole())) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"msg\":\"无操作权限\"}");
            return false;
        }
        request.setAttribute("userId", token.getUserId());
        request.setAttribute("userRole", token.getRole());
        return true;
    }

    // 仅SUPER_ADMIN可访问
    private boolean checkSuperAdminOnly(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\"}");
            return false;
        }
        UserToken token = authService.validateToken(auth.substring(7));
        if (token == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"登录已过期，请重新登录\"}");
            return false;
        }
        if (!"SUPER_ADMIN".equals(token.getRole())) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"msg\":\"仅超级管理员可操作\"}");
            return false;
        }
        request.setAttribute("userId", token.getUserId());
        request.setAttribute("userRole", token.getRole());
        return true;
    }
}