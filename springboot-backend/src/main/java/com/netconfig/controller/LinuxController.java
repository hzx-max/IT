package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import com.netconfig.dto.LinuxDTO;
import com.netconfig.service.LinuxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/linux")
@RequiredArgsConstructor
public class LinuxController {

    private final LinuxService linuxService;

    @GetMapping
    public List<LinuxDTO> list() {
        return linuxService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LinuxDTO> get(@PathVariable String id) {
        LinuxDTO dto = linuxService.findById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LinuxDTO>> create(@Valid @RequestBody LinuxDTO dto) {
        if (dto.getTitle() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing required fields"));
        }
        LinuxDTO created = linuxService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LinuxDTO>> update(@PathVariable String id, @RequestBody LinuxDTO dto) {
        LinuxDTO updated = linuxService.update(id, dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        linuxService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Map<String, Integer>> batchDelete(@RequestBody Map<String, List<String>> body) {
        List<String> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) return ApiResponse.error("ids required");
        linuxService.batchDelete(ids);
        return ApiResponse.success(Map.of("deleted", ids.size()));
    }
}
