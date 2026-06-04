package com.netconfig.controller;

import com.netconfig.dto.AiTopicDTO;
import com.netconfig.dto.ApiResponse;
import com.netconfig.service.AiTopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiTopicController {

    private final AiTopicService aiTopicService;

    @GetMapping
    public List<AiTopicDTO> list() {
        return aiTopicService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AiTopicDTO> get(@PathVariable String id) {
        AiTopicDTO dto = aiTopicService.findById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AiTopicDTO>> create(@Valid @RequestBody AiTopicDTO dto) {
        if (dto.getTitle() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing field: title"));
        }
        AiTopicDTO created = aiTopicService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AiTopicDTO>> update(@PathVariable String id, @RequestBody AiTopicDTO dto) {
        AiTopicDTO updated = aiTopicService.update(id, dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        aiTopicService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Map<String, Integer>> batchDelete(@RequestBody Map<String, List<String>> body) {
        List<String> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) return ApiResponse.error("ids required");
        aiTopicService.batchDelete(ids);
        return ApiResponse.success(Map.of("deleted", ids.size()));
    }
}