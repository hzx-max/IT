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
@RequiredArgsConstructor
public class CommandController {

    private final CommandService commandService;

    @GetMapping("/api/topics")
    public List<CommandDTO> list() {
        return commandService.findAll();
    }

    @GetMapping("/api/topics/{id}")
    public ResponseEntity<CommandDTO> get(@PathVariable String id) {
        CommandDTO dto = commandService.findById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/api/topics")
    public ResponseEntity<ApiResponse<CommandDTO>> create(@RequestBody CommandDTO dto) {
        if (dto.getTitle() == null || dto.getCat() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing title or cat"));
        }
        CommandDTO created = commandService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/api/topics/{id}")
    public ResponseEntity<ApiResponse<CommandDTO>> update(@PathVariable String id, @RequestBody CommandDTO dto) {
        CommandDTO updated = commandService.update(id, dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/api/topics/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        commandService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/api/topics/batch-delete")
    public ApiResponse<Map<String, Integer>> batchDelete(@RequestBody Map<String, List<String>> body) {
        List<String> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) return ApiResponse.error("ids required");
        commandService.batchDelete(ids);
        return ApiResponse.success(Map.of("deleted", ids.size()));
    }

    @GetMapping("/api/commands")
    public List<CommandDTO> listCommands() {
        return commandService.findAll();
    }

    @GetMapping("/api/commands/{id}")
    public ResponseEntity<CommandDTO> getCommand(@PathVariable String id) {
        CommandDTO dto = commandService.findById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/api/commands")
    public ResponseEntity<ApiResponse<CommandDTO>> createCommand(@RequestBody CommandDTO dto) {
        if (dto.getTitle() == null || dto.getCat() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing title or cat"));
        }
        CommandDTO created = commandService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/api/commands/{id}")
    public ResponseEntity<ApiResponse<CommandDTO>> updateCommand(@PathVariable String id, @RequestBody CommandDTO dto) {
        CommandDTO updated = commandService.update(id, dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/api/commands/{id}")
    public ApiResponse<Void> deleteCommand(@PathVariable String id) {
        commandService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/api/commands/batch-delete")
    public ApiResponse<Map<String, Integer>> batchDeleteCommands(@RequestBody Map<String, List<String>> body) {
        List<String> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) return ApiResponse.error("ids required");
        commandService.batchDelete(ids);
        return ApiResponse.success(Map.of("deleted", ids.size()));
    }
}
