package com.netconfig.controller;

import com.netconfig.dto.ApiResponse;
import com.netconfig.dto.DesktopDTO;
import com.netconfig.service.DesktopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/desktop")
@RequiredArgsConstructor
public class DesktopController {

    private final DesktopService desktopService;

    @GetMapping
    public List<DesktopDTO> list() {
        return desktopService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesktopDTO> get(@PathVariable String id) {
        DesktopDTO dto = desktopService.findById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DesktopDTO>> create(@RequestBody DesktopDTO dto) {
        if (dto.getTitle() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing field: title"));
        }
        DesktopDTO created = desktopService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DesktopDTO>> update(@PathVariable String id, @RequestBody DesktopDTO dto) {
        DesktopDTO updated = desktopService.update(id, dto);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        desktopService.delete(id);
        return ApiResponse.success();
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Map<String, Integer>> batchDelete(@RequestBody Map<String, List<String>> body) {
        List<String> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) return ApiResponse.error("ids required");
        desktopService.batchDelete(ids);
        return ApiResponse.success(Map.of("deleted", ids.size()));
    }
}
