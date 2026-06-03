package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import com.netconfig.entity.User;
import com.netconfig.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/auth/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户名和密码不能为空"));
        }
        if (username.length() < 3 || username.length() > 20) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户名长度3-20位"));
        }
        if (password.length() < 6) {
            return ResponseEntity.badRequest().body(ApiResponse.error("密码至少6位"));
        }
        User user = authService.register(username.trim(), password);
        if (user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户名已存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", user.getStatus(), "message", "注册成功，等待超级管理员审核")));
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户名和密码不能为空"));
        }
        Map<String, String> result = authService.login(username, password);
        if (result == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("用户名或密码错误，或账号未审核通过"));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String auth) {
        if (auth != null && auth.startsWith("Bearer ")) {
            authService.logout(auth.substring(7));
        }
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<ApiResponse<Map<String, String>>> me(@RequestHeader("Authorization") String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(ApiResponse.error("未登录"));
        }
        User user = authService.getCurrentUser(auth.substring(7));
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("登录已过期"));
        }
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "username", user.getUsername(),
                "role", user.getRole(),
                "status", user.getStatus()
        )));
    }

    @GetMapping("/api/auth/users")
    public ResponseEntity<ApiResponse<List<User>>> getUsers(@RequestHeader("Authorization") String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(ApiResponse.error("未登录"));
        }
        User current = authService.getCurrentUser(auth.substring(7));
        if (current == null || !"SUPER_ADMIN".equals(current.getRole())) {
            return ResponseEntity.status(403).body(ApiResponse.error("无权限"));
        }
        List<User> users = authService.getAllUsers();
        users.forEach(u -> u.setPassword(null)); // 不暴露密码
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping("/api/auth/approve/{userId}")
    public ResponseEntity<ApiResponse<User>> approveUser(
            @RequestHeader("Authorization") String auth,
            @PathVariable String userId,
            @RequestBody Map<String, Boolean> body) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(ApiResponse.error("未登录"));
        }
        User current = authService.getCurrentUser(auth.substring(7));
        if (current == null || !"SUPER_ADMIN".equals(current.getRole())) {
            return ResponseEntity.status(403).body(ApiResponse.error("无权限"));
        }
        boolean approved = body.getOrDefault("approved", true);
        User updated = authService.approveUser(userId, approved);
        if (updated == null) return ResponseEntity.notFound().build();
        updated.setPassword(null);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/api/auth/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @RequestHeader("Authorization") String auth,
            @PathVariable String userId) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(ApiResponse.error("未登录"));
        }
        User current = authService.getCurrentUser(auth.substring(7));
        if (current == null || !"SUPER_ADMIN".equals(current.getRole())) {
            return ResponseEntity.status(403).body(ApiResponse.error("无权限"));
        }
        authService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}