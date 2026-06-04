package com.netconfig.service;

import com.netconfig.entity.User;
import com.netconfig.entity.UserToken;
import com.netconfig.repository.UserRepository;
import com.netconfig.repository.UserTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();

    public User register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            return null;
        }
        User user = new User();
        user.setId("user_" + System.currentTimeMillis() + "_" + RANDOM.nextInt(10000));
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ADMIN");
        user.setStatus("PENDING");
        user.setCreatedAt(JsonUtil.nowLocal());
        return userRepository.save(user);
    }

    @Transactional
    public Map<String, String> login(String username, String password) {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) return null;
        User user = opt.get();
        if (!checkPasswordAndMigrate(user, password)) return null;
        if (!"APPROVED".equals(user.getStatus()) && !"SUPER_ADMIN".equals(user.getRole())) return null;

        // 删除旧token
        tokenRepository.deleteByUserId(user.getId());

        // 生成新token
        String token = generateToken();
        UserToken ut = new UserToken();
        ut.setToken(token);
        ut.setUserId(user.getId());
        ut.setRole(user.getRole());
        ut.setExpiresAt(LocalDateTime.now().plusDays(7).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        tokenRepository.save(ut);

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("id", user.getId());
        result.put("userId", user.getId());
        result.put("role", user.getRole());
        result.put("username", user.getUsername());
        return result;
    }

    public void logout(String token) {
        tokenRepository.deleteById(token);
    }

    public UserToken validateToken(String token) {
        if (token == null || token.isBlank()) return null;
        Optional<UserToken> opt = tokenRepository.findByToken(token);
        if (opt.isEmpty()) return null;
        UserToken ut = opt.get();
        try {
            if (LocalDateTime.parse(ut.getExpiresAt(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).isBefore(LocalDateTime.now())) {
                tokenRepository.deleteById(token);
                return null;
            }
        } catch (Exception e) {
            tokenRepository.deleteById(token);
            return null;
        }
        return ut;
    }

    public List<User> getPendingAdmins() {
        return userRepository.findByRoleOrderByCreatedAtDesc("ADMIN");
    }

    @Transactional
    public User approveUser(String userId, boolean approved) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) return null;
        User user = opt.get();
        user.setStatus(approved ? "APPROVED" : "REJECTED");
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String userId) {
        tokenRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    public User getCurrentUser(String token) {
        UserToken ut = validateToken(token);
        if (ut == null) return null;
        return userRepository.findById(ut.getUserId()).orElse(null);
    }

    public void migratePassword(User user, String rawPassword) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    private boolean checkPasswordAndMigrate(User user, String rawPassword) {
        String stored = user.getPassword();
        // 如果是 BCrypt 格式，直接匹配
        if (stored != null && stored.startsWith("$2")) {
            if (passwordEncoder.matches(rawPassword, stored)) return true;
            return false;
        }
        // 兼容旧版 SHA-256 哈希，匹配后自动迁移到 BCrypt
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawPassword.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            if (stored != null && stored.equals(sb.toString())) {
                user.setPassword(passwordEncoder.encode(rawPassword));
                userRepository.save(user);
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}