package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import com.netconfig.entity.User;
import com.netconfig.entity.UserProfile;
import com.netconfig.repository.UserRepository;
import com.netconfig.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("请先登录"));
        }

        Optional<User> userOpt = userRepository.findById(userId);
        Optional<UserProfile> profileOpt = profileService.getByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("username", request.getAttribute("username"));
        result.put("role", request.getAttribute("userRole"));

        if (userOpt.isPresent()) {
            User u = userOpt.get();
            result.put("status", u.getStatus());
            result.put("createdAt", u.getCreatedAt());
        }

        if (profileOpt.isPresent()) {
            UserProfile p = profileOpt.get();
            result.put("realName", p.getRealName());
            result.put("email", p.getEmail());
            result.put("avatar", p.getAvatar());
            result.put("bio", p.getBio());
            result.put("profileCreatedAt", p.getCreatedAt());
            result.put("profileUpdatedAt", p.getUpdatedAt());
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<UserProfile>> updateProfile(@RequestBody Map<String, String> body,
                                                                    HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("请先登录"));
        }

        UserProfile profile = profileService.saveOrUpdate(
                userId,
                body.get("realName"),
                body.get("email"),
                body.get("avatar"),
                body.get("bio")
        );
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
