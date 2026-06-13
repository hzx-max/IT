package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import com.netconfig.entity.Favorite;
import com.netconfig.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Favorite>>> list(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("请先登录"));
        }
        return ResponseEntity.ok(ApiResponse.success(favoriteService.getByUserId(userId)));
    }

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> check(@RequestBody Map<String, String> body,
                                                       HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("请先登录"));
        }
        boolean fav = favoriteService.isFavorite(userId, body.get("module"), body.get("itemId"));
        return ResponseEntity.ok(ApiResponse.success(fav));
    }

    @PostMapping("/toggle")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggle(@RequestBody Map<String, String> body,
                                                                    HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("请先登录"));
        }
        boolean added = favoriteService.toggle(
                userId,
                body.get("module"),
                body.get("itemId"),
                body.get("itemTitle"),
                body.get("moduleLabel"),
                body.get("description"),
                body.get("category"),
                body.get("itemPath")
        );
        return ResponseEntity.ok(ApiResponse.success(Map.of("added", added)));
    }
}
