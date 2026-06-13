package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import com.netconfig.entity.LearningNote;
import com.netconfig.service.LearningNoteService;
import com.netconfig.service.NoteModerationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning-notes")
@RequiredArgsConstructor
public class LearningNoteController {

    private final LearningNoteService learningNoteService;
    private final NoteModerationService noteModerationService;

    @GetMapping
    public ResponseEntity<?> getByTargetId(@RequestParam String targetId, HttpServletRequest request) {
        List<LearningNote> topLevel = learningNoteService.getByTargetId(targetId);
        String userId = (String) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        String role = (String) request.getAttribute("userRole");

        List<Map<String, Object>> result = new ArrayList<>();
        for (LearningNote note : topLevel) {
            Map<String, Object> noteMap = new java.util.HashMap<>();
            noteMap.put("id", note.getId());
            noteMap.put("targetId", note.getTargetId());
            noteMap.put("username", note.getUsername());
            noteMap.put("content", note.getContent());
            noteMap.put("likeCount", note.getLikeCount());
            noteMap.put("dislikeCount", note.getDislikeCount());
            noteMap.put("createdAt", note.getCreatedAt());
            noteMap.put("parentId", note.getParentId());
            noteMap.put("canEdit", username != null && username.equals(note.getUsername()));
            noteMap.put("canDelete", username != null && (username.equals(note.getUsername()) || "SUPER_ADMIN".equals(role)));

            List<LearningNote> replies = learningNoteService.getByParentId(note.getId());
            List<Map<String, Object>> replyList = new ArrayList<>();
            for (LearningNote reply : replies) {
                Map<String, Object> replyMap = new java.util.HashMap<>();
                replyMap.put("id", reply.getId());
                replyMap.put("targetId", reply.getTargetId());
                replyMap.put("username", reply.getUsername());
                replyMap.put("content", reply.getContent());
                replyMap.put("likeCount", reply.getLikeCount());
                replyMap.put("dislikeCount", reply.getDislikeCount());
                replyMap.put("createdAt", reply.getCreatedAt());
                replyMap.put("parentId", reply.getParentId());
                replyMap.put("canEdit", username != null && username.equals(reply.getUsername()));
                replyMap.put("canDelete", username != null && (username.equals(reply.getUsername()) || "SUPER_ADMIN".equals(role)));
                replyList.add(replyMap);
            }
            noteMap.put("replies", replyList);
            result.add(noteMap);
        }

        if (userId != null) {
            List<Long> allIds = new ArrayList<>();
            for (Map<String, Object> m : result) {
                allIds.add((Long) m.get("id"));
                for (Map<String, Object> r : (List<Map<String, Object>>) m.get("replies")) {
                    allIds.add((Long) r.get("id"));
                }
            }
            Map<Long, String> reactions = learningNoteService.getUserReactions(allIds, userId);
            for (Map<String, Object> m : result) {
                m.put("userReaction", reactions.get(m.get("id")));
                for (Map<String, Object> r : (List<Map<String, Object>>) m.get("replies")) {
                    r.put("userReaction", reactions.get(r.get("id")));
                }
            }
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String targetId = body.get("targetId");
        String content = body.get("content");
        if (targetId == null || targetId.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("targetId 不能为空"));
        }
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("笔记内容不能为空"));
        }

        NoteModerationService.ModerationResult modResult = noteModerationService.check(content);
        if (!modResult.isPassed()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(modResult.getMessage()));
        }

        String username = (String) request.getAttribute("username");
        if (username == null || username.isBlank()) {
            username = "游客";
        }

        LearningNote note = learningNoteService.create(targetId, username, content);
        return ResponseEntity.ok(ApiResponse.success(note));
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<?> reply(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        String content = body.get("content");
        String targetId = body.get("targetId");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("回复内容不能为空"));
        }

        NoteModerationService.ModerationResult modResult = noteModerationService.check(content);
        if (!modResult.isPassed()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(modResult.getMessage()));
        }

        String username = (String) request.getAttribute("username");
        if (username == null || username.isBlank()) {
            username = "游客";
        }

        LearningNote reply = learningNoteService.reply(id, targetId, username, content);
        return ResponseEntity.ok(ApiResponse.success(reply));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("请先登录"));
        }

        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("笔记内容不能为空"));
        }

        NoteModerationService.ModerationResult modResult = noteModerationService.check(content);
        if (!modResult.isPassed()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(modResult.getMessage()));
        }

        LearningNote updated = learningNoteService.updateById(id, username, content);
        if (updated == null) {
            return ResponseEntity.status(403).body(ApiResponse.error("只能编辑自己的笔记"));
        }
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> like(@PathVariable Long id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = (String) request.getAttribute("username");
        }
        if (userId == null) {
            userId = "guest_" + request.getRemoteAddr();
        }
        Map<String, Object> result = learningNoteService.like(id, userId);
        if (Boolean.FALSE.equals(result.get("ok"))) {
            return ResponseEntity.badRequest().body(ApiResponse.error((String) result.get("error")));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{id}/dislike")
    public ResponseEntity<?> dislike(@PathVariable Long id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            userId = (String) request.getAttribute("username");
        }
        if (userId == null) {
            userId = "guest_" + request.getRemoteAddr();
        }
        Map<String, Object> result = learningNoteService.dislike(id, userId);
        if (Boolean.FALSE.equals(result.get("ok"))) {
            return ResponseEntity.badRequest().body(ApiResponse.error((String) result.get("error")));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("请先登录"));
        }
        String role = (String) request.getAttribute("userRole");
        boolean deleted = learningNoteService.deleteById(id, username, role);
        if (!deleted) {
            return ResponseEntity.status(403).body(ApiResponse.error("只能删除自己的笔记"));
        }
        return ResponseEntity.ok(ApiResponse.success());
    }
}
