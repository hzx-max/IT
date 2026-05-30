package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import com.netconfig.dto.CommandDTO;
import com.netconfig.service.CommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commands")
@RequiredArgsConstructor
public class CommandController {

    private final CommandService commandService;

    @GetMapping
    public List<CommandDTO> list() {
        return commandService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandDTO> get(@PathVariable String id) {
        CommandDTO dto = commandService.findById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommandDTO>> create(@RequestBody CommandDTO dto) {
        if (dto.getId() == null || dto.getTitle() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing required fields"));
        }
        CommandDTO created = commandService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CommandDTO>> update(@PathVariable String id, @RequestBody CommandDTO dto) {
        CommandDTO updated = commandService.update(id, dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        commandService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Map<String, Integer>> batchDelete(@RequestBody Map<String, List<String>> body) {
        List<String> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) return ApiResponse.error("ids required");
        commandService.batchDelete(ids);
        return ApiResponse.success(Map.of("deleted", ids.size()));
    }
}
